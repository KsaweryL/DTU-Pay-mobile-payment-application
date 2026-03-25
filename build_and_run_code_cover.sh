#!/bin/bash
set -e

cleanup() {
	docker compose down --remove-orphans >/dev/null 2>&1 || true
}

wait_for_http() {
	local url="$1"
	local name="$2"
	local max_seconds="${3:-60}"

	for ((i=1; i<=max_seconds; i++)); do
		code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 1 "$url" || true)
		if [[ "$code" != "000" && -n "$code" ]]; then
			echo "$name is reachable at $url (HTTP $code)"
			return 0
		fi
		sleep 1
	done

	echo "Timed out waiting for $name at $url"
	docker compose ps || true
	docker compose logs --tail=200 "$name" || true
	return 1
}

trap cleanup EXIT

cleanup

mvn clean install -f libraries -DskipTests

mvn clean package -f services -DskipTests

docker compose build

docker compose up -d

wait_for_http "http://localhost:8080/merchant/register" "merchant-facade" 90
wait_for_http "http://localhost:8081/customer/register" "customer-facade" 90
wait_for_http "http://localhost:8082/manager/register" "manager-facade" 90
# wait_for_http "http://localhost:8081/customer/tokens/generate" "customer-facade" 10

# Run tests while services are up
mvn -f . test

# Dump JaCoCo data from Quarkus services (TCP server)
docker compose exec -T customer-facade java -jar /opt/jacococli.jar dump --address localhost --port 6301 --destfile /jacoco/jacoco.exec --reset || true
docker compose exec -T merchant-facade java -jar /opt/jacococli.jar dump --address localhost --port 6302 --destfile /jacoco/jacoco.exec --reset || true
docker compose exec -T manager-facade java -jar /opt/jacococli.jar dump --address localhost --port 6303 --destfile /jacoco/jacoco.exec --reset || true
docker compose exec -T bank-manager java -jar /opt/jacococli.jar dump --address localhost --port 6304 --destfile /jacoco/jacoco.exec --reset || true

# Stop services to flush JaCoCo exec data
cleanup

# Generate aggregate report via JaCoCo CLI using runtime classfiles
rm -rf target/site/jacoco-aggregate
mkdir -p target/site/jacoco-aggregate
curl -s -L -o /tmp/jacoco-cli.jar https://repo1.maven.org/maven2/org/jacoco/org.jacoco.cli/0.8.11/org.jacoco.cli-0.8.11-nodeps.jar

exec_files=()
while IFS= read -r -d '' f; do
	exec_files+=("$f")
done < <(find services -name jacoco.exec -type f -print0)

if [[ ${#exec_files[@]} -eq 0 ]]; then
	echo "No JaCoCo exec files found under services/." >&2
	exit 1
fi

merged_exec="target/jacoco-merged.exec"
cp "${exec_files[0]}" "$merged_exec"
for f in "${exec_files[@]:1}"; do
	java -jar /tmp/jacoco-cli.jar merge "$merged_exec" "$f" --destfile "$merged_exec"
done

classfiles=(
	target/jacoco-classfiles/account-manager
	target/jacoco-classfiles/payment-manager
	target/jacoco-classfiles/token-manager
	target/jacoco-classfiles/report-manager
	services/customer-facade/target/quarkus-app/app/customer-facade-1.0.0.jar
	services/merchant-facade/target/quarkus-app/app/merchant-facade-1.0.0.jar
	services/manager-facade/target/quarkus-app/app/manager-facade-1.0.0.jar
	services/bank-manager/target/quarkus-app/app/bank-manager-1.0.0.jar
)

sourcefiles=(
	services/account-manager/src/main/java
	services/payment-manager/src/main/java
	services/token-manager/src/main/java
	services/report-manager/src/main/java
	services/customer-facade/src/main/java
	services/merchant-facade/src/main/java
	services/manager-facade/src/main/java
	services/bank-manager/src/main/java
)

report_args=()
for cf in "${classfiles[@]}"; do report_args+=(--classfiles "$cf"); done
for sf in "${sourcefiles[@]}"; do report_args+=(--sourcefiles "$sf"); done

# Prepare classfiles for non-Quarkus modules and remove duplicate class names
classfiles_root="target/jacoco-classfiles"
rm -rf "$classfiles_root"
mkdir -p "$classfiles_root"

cp -r services/account-manager/target/classes/ "$classfiles_root/account-manager/" 2>/dev/null || true
cp -r services/payment-manager/target/classes/ "$classfiles_root/payment-manager/" 2>/dev/null || true
cp -r services/token-manager/target/classes/ "$classfiles_root/token-manager/" 2>/dev/null || true
cp -r services/report-manager/target/classes/ "$classfiles_root/report-manager/" 2>/dev/null || true

python3 - <<'PY'
import os

root = "target/jacoco-classfiles"
seen = set()

for dirpath, dirnames, filenames in os.walk(root):
	dirnames.sort()
	filenames.sort()
	for name in filenames:
		if not name.endswith(".class"):
			continue
		path = os.path.join(dirpath, name)
		rel = os.path.relpath(path, root)
		if os.sep in rel:
			class_rel = rel.split(os.sep, 1)[1]
		else:
			class_rel = rel
		if class_rel in seen:
			os.remove(path)
		else:
			seen.add(class_rel)
PY

java -jar /tmp/jacoco-cli.jar report "$merged_exec" \
	"${report_args[@]}" \
	--html target/site/jacoco-aggregate \
	--xml target/site/jacoco-aggregate/jacoco.xml \
	--csv target/site/jacoco-aggregate/jacoco.csv

if [[ ! -s target/site/jacoco-aggregate/index.html ]]; then
	echo "JaCoCo report generation failed; index.html not created" >&2
	exit 1
fi

docker image prune -f 
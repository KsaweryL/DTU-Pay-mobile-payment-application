# Git hooks

This folder contains helper scripts to install/remove the local Git `pre-push` hook.

## macOS/Linux
- Install: `chmod +x git-hooks/*.sh && ./git-hooks/activate-git-pre-push.sh`
- Remove: `chmod +x git-hooks/*.sh && ./git-hooks/deactivate-git-pre-push.sh`

## Windows (PowerShell)
- Install: `./git-hooks/activate-git-pre-push.ps1`
- Remove: `./git-hooks/deactivate-git-pre-push.ps1`

The `pre-push` hook runs `build_and_run.sh`. Set `SKIP_PRE_PUSH` or `SKIP_PRE_PUSH_HOOK` to skip.


## One-time skip:
To push without running the `pre-push` hook just once, use either:
- `SKIP_PRE_PUSH=1 git push`

or

- `SKIP_PRE_PUSH_HOOK=1 git push`

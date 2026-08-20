# Claude Code — configuration scopes

`settings.json` can live at different scopes. Each scope controls who the
settings apply to and whether they're shared with a team.

| Scope | File location | Shared? | Use for |
|---|---|---|---|
| **Enterprise / managed** | OS-managed policy location (set by an admin/MDM) | Org-wide, can't be overridden | Company-wide security policy (e.g. blocked tools) |
| **User (global)** | `~/.claude/settings.json` | Just you, across all projects | Your personal preferences (permissions, theme, default model) |
| **Project (shared)** | `<repo>/.claude/settings.json` | Checked into git, shared with the team | Settings the whole team should use for this repo (allowed commands, hooks) |
| **Project (local)** | `<repo>/.claude/settings.local.json` | Not checked in (usually gitignored) | Your personal overrides for this specific repo, without affecting teammates |

## Precedence

Higher wins when settings conflict:

```
Enterprise (managed policy)
    > Local project (.claude/settings.local.json)
    > Shared project (.claude/settings.json)
    > User / global (~/.claude/settings.json)
```

A team-wide rule in the shared project file can be overridden just for you via
the local file, but nothing can override an enterprise-managed policy.

## What scoping applies to

Most things `settings.json` controls follow this same scope system:

- Permissions / allowlisted commands
- Environment variables
- Hooks
- MCP server definitions

## Example

Want to always-allow `npm run *` just for yourself in one repo, without
affecting teammates? Put it in `.claude/settings.local.json`. Want the whole
team to have it? Put it in `.claude/settings.json` and commit it.

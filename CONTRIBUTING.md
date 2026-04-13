## Development

Each day has its own branch holding the expected state of the application at the end of that day.
This allows for easy navigation and comparison between days using `git switch` and `git diff`.

```shell
git switch day-01
```

```shell
git diff day-01 day-02
```

Git worktrees can help you maintain multiple branches as directories.
This allows you to easily update the guide or code of a specific day without switching branches.

```shell
git worktree add day-01 day-01
```

## Change requests

If you want to suggest changes to the guide, please open a pull request against the `main` branch.
Make sure to follow the conventions outlined in this document and provide a clear description of your changes.
There is no need to open separate pull requests for changes to the code in the checkpoint branches.
These branches are updated manually by a maintainer to reduce noise and ease access for contributors.

You may also open an issue if you have questions or suggestions that are not directly related to a specific change.

## Content

Days are plain Markdown first and should render well both on GitHub and in VitePress.

Each day should include links to download the starting code at the beginning and the result at the end.

- [Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-01.zip)
- [Download result](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-02.zip)

```markdown
**Goal**:

- First goal
- Second goal

**Estimated time**: 2h
```

Refer to UI elements by highlighting its label in **bold**.
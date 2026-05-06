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

1. Days start with a checklist of goals, an estimated time to complete the day, and a link to download the starting code.
2. The guide itself is best grouped into a few steps providing a clear outline.
3. At the end of the day, there is a checkpoint section that summarises the key takeaways.
4. A final section outlines what the next day will cover and provides a link to download the expected result of the day.

The following structure can be used as a template for new day guides:

```markdown
# Day 1 — A descriptive title for the day

**Goal**:

- First goal
- Second goal

**Estimated time**: 2h

[Download starting code](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-01.zip)

## Step 1 — The first of a few steps

We will build ...
Create the file ...

## Checkpoint

- First key takeaway
- Second key takeaway

## What's next

Next we will cover...

[Download the result of the day](https://github.com/eddie-energy/tutorial/archive/refs/heads/day-01.zip)
```

Use British English.
Refer to UI elements by highlighting its label in **bold**.

Code snippets should include the file name in square brackets after the language.
Exclude the package name and imports.
Include imports for symbols with multiple reasonable implementation options.

````markdown
```java [SomeClass.java]
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
class UserController {

    @GetMapping("/api/me")
    Map<String, String> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("id", jwt.getSubject(), "name", jwt.getClaimAsString("name"));
    }
}
```
````

Refer to Java files by file name when the file is first created and by class name if it already exists.
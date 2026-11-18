# AI Use Disclosure

AI assistance was used to review the requirements and help implement
the REPL, multiline input handling, related tests, and usage documentation.
The project test suite and a REPL smoke test were run to verify the changes.
The project authors should review this disclosure and update it to reflect
their course policy and each team member's use of AI.

## Conversation Log

**User:** How many sections will be left after this second commit?

**Agent:** Two sections are left after the second commit:

- Section 3, output: the JSON converter (toJSON), which turns a parsed tree into nested JSON, and the unparser, which turns a tree back into indented MiniJS source.
- Section 4, the REPL and testing: the interactive loop that prompts, echoes the input, and prints the tree, the JSON, and the unparsed source. It also includes the jline and multi-line input extra credit, plus round-trip tests checking that parsing the unparsed output gives back the same tree

**User:** Gives agent assignment details, are all assignment requirement met, is there anything we may have missed?

**User:** Based on these requirements (pasted), does the code fulfill them all, this is including the optional extra credit requirement.

**Agent:**

This is a fresh machine, so sbt is gone. I'll reinstall it outside the repo and rerun the tests.

All 92 tests pass with no compiler warnings. Next I'll drive the actual REPL through sbt run in a terminal session and test the spec's example, a malformed input, multi-line input, and exiting.

The REPL works with the instructor's example. Some of my keystrokes didn't go through because of how I sent them, so I'll resend the remaining inputs one key at a time.

Worth fixing (small code issues)

- Ctrl-C crashes the REPL with a Java stack trace (UserInterruptException). A short fix lets it cancel the current input instead.
- A blank line at the prompt runs an empty program, printing Block(List()) and empty JSON. It should just show the prompt again.
- README typo: "output form the sbt run" should be "output from".
- Stray blank line in ast.scala: Blake's commit added one between the Statement doc comment and the enum, so the comment no longer attaches to it.

**User:** Could you implement the REPL evaluation and see how the memory behavior is changed through the REPL?

**Agent:** I reviewed the REPL tests and the existing evaluator, then added
evaluation output and reused one store across REPL inputs. I expanded the
tests to cover result output, memory persistence, and continuing after parse
or runtime failures. An initial test failure showed that the expected error
message needed to include the input's semicolon; after correcting
the expectation, all 123 tests passed.

**User:** are all these requirements met (instructions pasted) and is this assignemnet ready for submission?

**Agent:** Not quite. The interpreter itself meets every requirement, but the REPL doesn't print memory before and after each program, which the spec explicitly requires. A few submission steps are also still open.

Blake pushed "Added the REPL tests and outputs" on Nov 16 at 9:56 AM CST. It connects the interpreter to the REPL, keeps one memory across inputs, and adds five REPL tests. Your local `main` matches GitHub. All 123 tests pass with no warnings, and I ran the spec's sample session in the real REPL.

*Requirements*

| Criterion | Points | Status |
|---|---|---|
| Variable | 1 | Met. Variables are looked up in memory, and an unknown name fails with `NoSuchFieldException`. |
| Assignment | 1 | Met. Assigning creates or updates the variable and returns void (`Num(0)`). |
| Conditional | 0.5 | Met. Nonzero runs `then`, zero runs `else`, and the result is the chosen branch's value. |
| Loop | 0.5 | Met. The loop repeats while the guard is nonzero and returns void. |
| Block | 1 | Met. A block returns its last statement's value. |
| Testing | 2 | Met. 123 tests cover each construct, the spec examples, failure cases, and the REPL. |
| REPL integration | 1 | Partly met (see below). |

Also met:

- Top-level `Try`: failures are caught there, so the REPL keeps running after a runtime error.
- Memory between inputs: variables persist from one input to the next.
- JavaScript consistency: final memory matches Node.js for all 20 programs I compared.

*What the REPL is missing*

Here's what it actually printed for the spec's examples, compared with what the spec shows:

| | Spec | Our REPL |
|---|---|---|
| Before the prompt | `Memory: Map()` | nothing |
| Result | `It evaluates to Success(Num(0))` | `The result is:` then `Num(0)` |
| Failure on `z;` | `It evaluates to Failure(java.lang.NoSuchFieldException: z)` | `Error in input 'z;': z` |
| After evaluating | `Memory: Map(y -> Num(0), x -> Num(2), r -> Num(6))` | nothing |

1. No memory printing. The spec says "your REPL should print the mutable store before and after interpreting a program." This is the clearest gap, and it's likely to cost part of the REPL integration point.
2. Result format differs from the spec. The failure message is the bigger problem: it shows only `z` and drops the exception type, so it's hard to tell what went wrong.
3. Small: the import of `Success`/`Failure` in `MiniJSRepl.scala` has the comment `//used for the tests`, but it's actually used by the REPL code.

*Submission steps still open*

- Make the repo private. It's still public.
- Create the `p3b` tag, or note the 7-digit commit hash, once the REPL fix is in.
- Submit on Sakai with the repo URL, the tag or hash, and both team members' names.
- Optional, your call: the AI disclosure covers Blake's REPL work but not the interpreter we built in Section 1.

*Recommended fix*

The fix is small and only touches `MiniJSRepl.scala` and its tests:

- Print `Memory: Map(...)` before each prompt and again after evaluating.
- Print `It evaluates to Success(...)` or `It evaluates to Failure(...)`, matching the spec exactly.
- Update the five REPL tests to match, and add a test that replays the spec's sample session.
- Fix the misleading comment.

If you want, I'll make these changes and show them to you without committing. After that, the only remaining steps would be committing, tagging `p3b`, making the repo private, and the Sakai submission.

**User:** Please complete, "Recommended fix
The fix is small and only touches MiniJSRepl.scala and its tests:

Print Memory: Map(...) before each prompt and again after evaluating.
Print It evaluates to Success(...) or It evaluates to Failure(...), matching the spec exactly.
Update the five REPL tests to match, and add a test that replays the spec's sample session.
Fix the misleading comment."

**Agent:** I've made the recommended fix: the REPL now prints memory and results in the spec's format, and all 126 tests pass with no warnings. I haven't committed anything; the changes are in `MiniJSRepl.scala` and `TestBehaviors.scala`.

*What changed*

- Memory printing: `Memory: Map()` prints once at startup, and every input's output ends with the updated memory. That way the current memory always sits just above the next prompt, covering both "before" and "after" without repeating lines.
- Result format:
  - Results now read `It evaluates to Success(Num(7))` or `It evaluates to Failure(java.lang.NoSuchFieldException: z)`, exactly as in the spec.
  - This replaces Blake's `The result is:` line and his `Error in input …` message, which hid the exception type.
- Comment: removed the `Success`/`Failure` import and its misleading `//used for the tests` comment; the new code doesn't need it.

*Tests*

- The five existing REPL tests now check the new result line and the memory line. That includes failures leaving earlier assignments in place, and parse errors leaving memory unchanged.
- Three new tests:
  - A replay of the spec's sample sessions: `x = 3;`, `{ x = 7; x; }`, the `while` loop, then `z;` failing with memory unchanged.
  - Memory display with six variables.
  - Alphabetical ordering of variables.

I also ran the same session in the real REPL with `sbt run`, and the output matches the spec's samples.

*One difference from the spec*

The spec's sample shows `Memory: Map(y -> Num(0), x -> Num(2), r -> Num(6))`, but ours shows `Map(r -> Num(6), x -> Num(2), y -> Num(0))`. The values are identical. The spec's order is just how the instructor's hash map happened to store the keys, so I sort variables by name to keep the output predictable.

*Ready to commit when you say so*

Suggested message: `Print memory and evaluation result in REPL`.

As before, it would go on `main`, authored as you with no co-author line. After that, the remaining steps are:

- Push.
- Tag `p3b`.
- Make the repo private (it's still public).
- Submit on Sakai.

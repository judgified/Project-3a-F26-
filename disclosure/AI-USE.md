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

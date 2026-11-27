# Money Tracker — Phase 0 Execution Checklist

**Status:** Not started  
**Prepared:** 2026-11-26  
**Owner:** Solo developer  
**Developer profile:** Intermediate; Windows; Kotlin preferred; 15–25 hours/week  
**Initial audiences to investigate:** Students and working professionals  
**Phase duration:** Approximately 2 weeks (30–50 focused hours), extend to 3 weeks if recruiting requires it.

---

## 1. Phase 0 outcome

Before implementation begins, produce evidence-backed answers to these questions:

1. Who experiences the problem most frequently and painfully?
2. What do they currently do when they want to understand or control their spending?
3. What specifically fails in their current approach?
4. What outcome would make them try and continue using a new product?
5. What data sources would they realistically trust and enable?
6. What is the smallest product experience that can test the strongest value proposition?

**Exit condition:** A written decision on the first target segment, a concise problem statement, a ranked set of user needs, an MVP hypothesis, and a plan for testing it. Do not treat interview enthusiasm or hypothetical “I would use that” answers as proof of demand.

---

## 2. Working principles

- Investigate actual past behaviour, not only opinions about a proposed app.
- Do not pitch Money Tracker at the beginning of interviews.
- Ask about the last real incident, the workaround used, and the consequences.
- Compare students and working professionals; do not assume they are one persona.
- Separate facts, interpretations, and hypotheses in research notes.
- Do not collect bank credentials, UPI PINs, OTPs, CVVs, account numbers, or unnecessary personal financial details.
- Obtain permission before recording or retaining interview notes; anonymize notes.
- Do not promise complete transaction capture, guaranteed savings, or accurate automatic categorization.
- Keep the first experiment manual/concierge if that can test the value proposition without building the app.
- No implementation commitment until the Phase 0 exit review.

---

## 3. Work plan and checklist

### Stage 0.1 — Set up the research workspace (2–3 hours)

- [ ] Create a `research/` folder in the project repository.
- [ ] Create `research/README.md` explaining the research purpose and privacy rules.
- [ ] Create an anonymized participant ID convention (for example `S01` for student, `W01` for working professional).
- [ ] Create an interview notes template (see Section 5).
- [ ] Create a hypothesis register (see Section 6).
- [ ] Create a decision log entry for Phase 0.
- [ ] Create a simple spreadsheet to track recruitment, interviews, evidence, and follow-ups.
- [ ] Set up a Git repository if one does not already exist; do not put identifiable interview data in a public repository.

**Deliverables:** Research workspace, participant tracker, interview template, hypothesis register.

### Stage 0.2 — Define the initial hypotheses (2–3 hours)

Write these as hypotheses, not facts:

- [ ] H1: People lose visibility into spending because transactions are spread across multiple payment methods/apps.
- [ ] H2: Manual expense entry is too much effort for many potential users to sustain.
- [ ] H3: Users value useful explanations and timely insights more than a ledger that merely stores transactions.
- [ ] H4: Some users would trust an opt-in, on-device method of reading transaction notifications.
- [ ] H5: Students and working professionals have meaningfully different spending patterns, pain points, and willingness to adopt.
- [ ] H6: A weekly summary or a specific timely insight could create recurring value.
- [ ] H7: Users will accept some setup effort if the product gives them a useful result immediately.

For each hypothesis:
- [ ] Define what evidence would support it.
- [ ] Define what evidence would weaken or disprove it.
- [ ] Avoid changing the hypothesis after each interview merely to make it appear validated.

**Deliverable:** Hypothesis register with evidence criteria.

### Stage 0.3 — Recruit participants (3–5 hours)

Target **8–12 interviews** initially:
- [ ] Recruit 4–6 students.
- [ ] Recruit 4–6 working professionals.
- [ ] Include a mix of people who track expenses, tried and abandoned tracking, and never track.
- [ ] Avoid interviewing only close friends who may tell you what you want to hear.
- [ ] Ask for 20–30 minutes, explain the purpose neutrally, and make participation optional.
- [ ] Do not ask participants to share statements, screenshots, account details, or sensitive transaction data during discovery.

Recruitment message:

> I'm researching how people keep track of everyday spending and understand where their money goes. I'm not selling anything, and I'm not asking for bank details or financial documents. Would you be open to a 20–30 minute conversation about how you currently handle your expenses?

- [ ] Track invitations, confirmations, completed interviews, and participant group.
- [ ] If one group is difficult to recruit, record that limitation rather than silently replacing the sample.

**Deliverable:** 8–12 completed interviews or a documented recruitment limitation.

### Stage 0.4 — Conduct problem interviews (8–12 hours)

Aim for 20–30 minutes per interview. Use the interview guide in Section 4.

During each interview:
- [ ] Ask about the most recent real occasion they were surprised by their spending or needed to understand it.
- [ ] Ask what triggered the concern.
- [ ] Ask them to walk through what they actually did.
- [ ] Identify apps, notes, spreadsheets, bank statements, mental estimates, or other workarounds.
- [ ] Ask what was frustrating, slow, confusing, or missing.
- [ ] Ask how often this happens and what consequences it has.
- [ ] Ask whether they tried an expense tracker or budgeting app; if they stopped, ask why.
- [ ] Ask what they currently do to detect subscriptions or recurring payments.
- [ ] Ask what they would not want an expense app to access or do.
- [ ] Ask what would make them uninstall an app.
- [ ] Record direct quotes only with permission and without identifying details.
- [ ] Do not lead with features such as AI, automatic tracking, alerts, or notification access.

**Deliverable:** Anonymized notes for every completed interview.

### Stage 0.5 — Synthesize the evidence (4–6 hours)

- [ ] Review all notes after interviews, not just the most memorable conversations.
- [ ] Group repeated problems into themes.
- [ ] Count how many participants independently mentioned each theme; keep student and professional counts separate.
- [ ] Distinguish frequent inconvenience from high-impact pain.
- [ ] Identify current workarounds and what they reveal about user motivation.
- [ ] Record reasons people do not track expenses.
- [ ] Record privacy and trust objections in participants' own terms.
- [ ] Identify contradictory evidence and people who do not experience the problem.
- [ ] Separate observed facts from your interpretation.
- [ ] Select 2–3 candidate user segments and compare them.

Use this evidence table:

| Theme | Students (n) | Professionals (n) | Evidence / quote (anonymized) | Severity | Confidence |
|---|---:|---:|---|---|---|
| Example: month-end surprise | | | | | |
| Example: manual entry fatigue | | | | | |
| Example: fragmented payment sources | | | | | |
| Example: subscription uncertainty | | | | | |
| Example: privacy concerns | | | | | |

**Deliverable:** Research synthesis with themes, counterexamples, and segment comparison.

### Stage 0.6 — Choose the first target segment (2–3 hours)

Compare candidate segments using evidence, not personal preference.

Score each from 1 (weak) to 5 (strong), and add a short evidence note:
- [ ] Problem frequency.
- [ ] Problem severity / consequence.
- [ ] Existing workaround dissatisfaction.
- [ ] Ease of reaching and recruiting users.
- [ ] Likelihood of trying an early product.
- [ ] Ability to test value without integrations.
- [ ] Fit with a solo developer's available time and resources.

- [ ] Choose one primary segment for the first MVP experiment.
- [ ] Keep the other segment as a later expansion candidate.
- [ ] Document why the selected segment wins and what evidence could change the decision.

**Deliverable:** One-page initial persona/segment decision. This is a provisional decision, not a permanent commitment.

### Stage 0.7 — Test the value proposition without building the app (4–6 hours)

Choose one low-cost experiment that tests whether the outcome matters:

**Option A — Concierge spending review**
- [ ] Invite 3–5 willing participants to use a statement/export they choose to provide, after removing sensitive details.
- [ ] Prefer a small, manually prepared sample or participant-entered totals; do not request credentials.
- [ ] Prepare a simple spending summary and 2–3 potentially useful observations.
- [ ] Clearly label uncertain categories and possible recurring payments.
- [ ] Ask which observations were genuinely useful, which were wrong, and what action they took.
- [ ] Delete source material according to the agreed retention plan.

**Option B — Clickable or paper prototype**
- [ ] Sketch the minimum flow: setup → first useful result → review/correction → recurring value.
- [ ] Show it only after the problem interview.
- [ ] Ask the participant to complete a task and narrate their thinking.
- [ ] Observe confusion; do not explain the interface unless necessary.
- [ ] Ask what they would expect to happen next.

**Option C — Weekly insight test**
- [ ] Agree on a small, privacy-conscious set of user-provided spending totals.
- [ ] Prepare one weekly summary manually.
- [ ] Ask whether it changed understanding or behaviour.
- [ ] Test whether the participant wants another summary the following week.

- [ ] Pick one experiment; do not build all three.
- [ ] Define the success signal before running it.
- [ ] Record negative feedback and no-shows as evidence too.

**Deliverable:** Experiment report with method, participants, observations, limitations, and decision.

### Stage 0.8 — Define the MVP boundary (2–3 hours)

Based on evidence:
- [ ] Write the single core job the MVP will help the selected user accomplish.
- [ ] Define the first useful result the user should get.
- [ ] List must-have capabilities only.
- [ ] List explicitly excluded features.
- [ ] Decide whether the first MVP needs manual entry, CSV import, notification capture, or another input method; justify from user evidence.
- [ ] Define what the app will say when data is incomplete.
- [ ] Define how users correct wrong merchants/categories.
- [ ] Define minimum privacy expectations and consent moments.
- [ ] Define one activation metric and one retention/value metric.

Potential features such as notification capture, AI chat, email access, bank integrations, budgets, and subscription detection are not automatically MVP requirements. Include only those needed to test the validated user problem.

**Deliverable:** MVP scope and measurable product hypothesis.

### Stage 0.9 — Phase 0 decision review (1–2 hours)

- [ ] Review all deliverables.
- [ ] Check that every major product decision has supporting evidence or is explicitly labelled an assumption.
- [ ] Identify unresolved risks.
- [ ] Decide: proceed to Phase 1, run another discovery experiment, narrow the segment further, or reconsider the problem.
- [ ] Update `PROJECT_CONTEXT.md`, `ROADMAP.md`, `DECISIONS.md`, and `PROGRESS_LOG.md`.
- [ ] Do not mark Phase 0 complete if interviews were conducted but no synthesis or decision was made.

**Deliverable:** Phase 0 decision record: proceed / iterate / pivot, with rationale.

---

## 4. Interview guide

### Opening (2 minutes)

"Thanks for speaking with me. I'm researching how people handle everyday spending. I'm interested in what you actually do today, including things that don't work. There are no right answers. Please don't share account numbers, credentials, OTPs, or anything sensitive."

### Core questions

1. Think about the last time you wondered where your money had gone. What happened?
2. What made you notice or care at that moment?
3. What did you do next, step by step?
4. Where did you look for the information?
5. What was difficult or missing?
6. How did you decide whether your spending was okay or too high?
7. Do you record expenses? Tell me about the last time you did.
8. Have you tried an expense tracker or budgeting app? What happened after the first few days or weeks?
9. What do you currently do about subscriptions or repeated payments?
10. When was the last time a payment or expense surprised you?
11. What financial information would you be uncomfortable allowing an app to process?
12. What would make you stop using an app that claims to help with spending?

### Closing questions

13. If you could improve one part of your current process, what would it be?
14. Is there something about your spending routine I haven't asked that matters?

Avoid asking "Would you use my app?" as the main validation question. If you do show a concept, ask for concrete feedback on the task and request a small next action (for example, agreeing to test a prototype), not a hypothetical promise.

---

## 5. Interview notes template

```text
Participant ID:
Group: Student / Working professional
Interview date:
Consent for notes: Yes / No
Consent for recording: Yes / No (optional)

Recent real incident:
Trigger:
What they did (step by step):
Tools / workarounds:
Pain points:
Frequency:
Consequences:
Past apps tried:
Why they continued / stopped:
Trust and privacy boundaries:
What would make them uninstall:
Direct anonymized quotes:
Surprising evidence:
Contradictory evidence:
Researcher interpretation (keep separate from facts):
Follow-up opportunity:
```

Do not store names, phone numbers, account details, or raw financial documents in the research notes.

---

## 6. Hypothesis register template

| ID | Hypothesis | Evidence that supports it | Evidence that weakens it | Status |
|---|---|---|---|---|
| H1 | Spending visibility is fragmented | | | Untested |
| H2 | Manual entry is a major adoption barrier | | | Untested |
| H3 | Insights are more valuable than a ledger alone | | | Untested |
| H4 | Users may trust a consent-based local-first capture approach | | | Untested |
| H5 | Student and professional needs differ | | | Untested |
| H6 | A weekly or timely insight creates recurring value | | | Untested |
| H7 | Immediate value justifies setup effort | | | Untested |

Use statuses such as `Untested`, `Supported`, `Mixed`, `Weakened`, or `Rejected`. A hypothesis is not “validated” merely because one participant agrees.

---

## 7. Suggested two-week schedule

This is a planning guide, not a deadline that should compromise research quality.

| Work block | Estimated effort | Output |
|---|---:|---|
| Workspace and hypotheses | 3–4 h | Research setup |
| Recruitment | 3–5 h | Participant pipeline |
| Interviews | 8–12 h | 8–12 interview notes |
| Synthesis and segment comparison | 4–6 h | Evidence summary |
| Value experiment | 4–6 h | Experiment report |
| MVP definition and decision review | 3–4 h | Phase 0 decision |
| **Total** | **25–37 h** | **Phase 0 package** |

At 15–25 hours/week, this is approximately 1.5–2 weeks of focused work, plus recruiting delays.

---

## 8. Phase 0 completion criteria

Phase 0 is complete only when all are true:

- [ ] 8–12 interviews completed, or a clear documented reason and limitation if fewer.
- [ ] Both students and working professionals represented in the research.
- [ ] Evidence synthesis includes themes and counterexamples.
- [ ] A first target segment is selected with written reasoning.
- [ ] At least one low-cost value experiment is completed or a justified reason for deferring it is recorded.
- [ ] The core user job and value proposition are written in plain language.
- [ ] MVP inclusions and exclusions are documented.
- [ ] Initial success metrics are defined.
- [ ] Privacy and trust expectations are recorded.
- [ ] Project context, decisions, roadmap, and progress log are updated.
- [ ] A proceed / iterate / pivot decision is recorded.

### Example decision statement

"We will proceed to Phase 1 because [specific evidence]. Our first target segment is [segment] because [observed pain and reachable users]. The MVP will test [single core job] using [minimum input method]. We will not build [excluded features] until [evidence or condition]. Our biggest unresolved risk is [risk]."

---

## 9. What not to build during Phase 0

- [ ] No Kotlin app implementation.
- [ ] No notification listener.
- [ ] No database/backend selection beyond recording constraints.
- [ ] No Firebase or Supabase setup.
- [ ] No Gemini/LLM integration.
- [ ] No Account Aggregator integration.
- [ ] No Gmail or WhatsApp integration.
- [ ] No Play Store or APK distribution work.

These may become relevant later. Phase 0 should establish whether they are necessary, not assume they are.

---

## 10. Immediate next action

Start with Stage 0.1 and Stage 0.2. Then recruit the first 8–12 participants before presenting a product concept. Keep a record of what people actually do, not just what they say they want.

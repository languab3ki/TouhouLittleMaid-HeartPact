# Generic Maid Interaction Framework Plan

## Goal

Build a reusable maid interaction framework without disturbing the existing `HugActionScreen`.

The first user of the framework is the little maid spirit system. The framework must also be usable by future addon mods, including addons that only provide JSON dialogue resources and no Java code.

## What We Keep From The Current Hug UI

The current `HugActionScreen` already has several good parts that should be reused instead of rewritten:

- `DialogueBoxComponent`, `DialogueOptionComponent`, `DialoguePortraitComponent`, and `DialogueIconButtonComponent` already separate rendering from screen logic.
- `DialogueTheme`, `DialogueThemeLoader`, and `DialogueThemeFileStore` give us percent-based layout, theme JSON, and local layout overrides.
- `DialogueScenario`, `DialogueScenarioLoader`, and `DialogueSessionController` already support data-driven nodes, choices, branches, events, conditions, and action queues.
- `DialogueRuntimeContext` already stores variables and action requests in a safe way.
- `DialogueEventRegistry` already avoids arbitrary Java execution from JSON.
- The current screen already solved useful UX details: typewriter text, option cards, portrait expressions, close/hide controls, debug layout overlay, and robust template rendering.

## Problems To Avoid

- Do not make another hardcoded one-off `SpiritActionScreen`.
- Do not put spirit logic into `HugActionScreen`.
- Do not force addon authors to edit main mod Java just to add text or options.
- Do not allow JSON to execute arbitrary code or commands.
- Do not trust PowerShell mojibake. Keep source and JSON UTF-8.

## New Architecture

### 1. `GenericMaidInteractionScreen`

This is the reusable UI container.

Responsibilities:

- Render dialogue box, portrait, option cards, and control buttons.
- Drive a `DialogueSessionController`.
- Apply `DialogueTheme` and existing layout/debug behavior.
- Handle mouse and keyboard input.
- Render current frame from a scenario.
- Send selected actions to `InteractionActionRegistry`.

It must not know whether the target is an adult maid, child maid, spirit, or addon entity.

### 2. `InteractionSession`

A small immutable runtime description of a currently opened interaction.

Fields:

- `targetType`: `ResourceLocation`, for example `maidmarriage:spirit`.
- `targetUuid`: target entity UUID.
- `scenarioId`: selected scenario.
- `title`: screen title.
- `allowVoice`: whether voice replay is enabled.
- `closeAction`: optional action id or callback behavior.

### 3. `InteractionTargetAdapter`

Adapter for a target entity type.

Responsibilities:

- Decide whether this adapter supports an entity.
- Return stable target display name.
- Write target-specific variables into the dialogue runtime.
- Validate whether the screen should remain open.
- Select default scenario id.

Example target types:

- `maidmarriage:spirit`
- Future: `maidmarriage:adult_maid`
- Future: `maidmarriage:child_maid`
- Addon: `othermod:special_maid`

### 4. `InteractionTargetRegistry`

Client-side registry for adapters.

Responsibilities:

- Register target adapters by `ResourceLocation`.
- Resolve an entity into an adapter.
- Open a generic interaction screen for the resolved target.

The first registered adapter will be `maidmarriage:spirit`.

### 5. `InteractionActionRegistry`

Safe action registry.

Responsibilities:

- Map semantic action ids to Java handlers.
- Execute action requests emitted by dialogue JSON.
- Refuse unknown actions gracefully and show a debug message.

Example actions:

- `maidmarriage:spirit_soothe`
- `maidmarriage:close_interaction`
- Future/addon actions such as `othermod:start_quest`.

JSON only stores action ids and params. It never stores Java class names.

### 6. `InteractionVariableProvider`

Optional extension point for addons to add variables.

Responsibilities:

- Add variables such as `has_item`, `festival_active`, or `quest_completed`.
- Keep variables string-based so the existing `SimpleDialogueConditionEvaluator` continues to work.

For phase one, target adapters can write variables directly. A separate provider registry can be added after the spirit flow is stable.

### 7. Scenario Loading

Existing localized scenario loading is good and should be reused.

Phase one:

- Use explicit scenario ids from adapters.
- Put spirit scenario at:
  - `assets/maidmarriage/dialogue/scenarios/spirit_interaction_v1.json`
  - `assets/maidmarriage/dialogue/zh_cn/scenarios/spirit_interaction_v1.json`
  - `assets/maidmarriage/dialogue/en_us/scenarios/spirit_interaction_v1.json`
  - `assets/maidmarriage/dialogue/ja_jp/scenarios/spirit_interaction_v1.json`

Phase two:

- Add a manifest loader for addon scenarios:
  - `assets/<modid>/maid_interactions/scenarios/*.json`
  - or `assets/<modid>/maid_interactions/manifest.json`
- Manifest fields can include `targetType`, `priority`, `conditions`, and `scenario`.

The reason phase one uses explicit ids is robustness. Minecraft resource listing APIs vary by client lifecycle, while direct resource loading is already proven in the current codebase.

## Spirit Phase One Behavior

Opening:

- Existing unified interaction key checks crosshair target.
- If target is `MaidSpiritEntity`, open `GenericMaidInteractionScreen` through `InteractionTargetRegistry`.
- Spirit must be handled before normal `EntityMaid` logic because it extends `EntityMaid`.

Scenario:

- Prompt explains that the spirit is lingering.
- Options:
  - `安抚 / Soothe / なだめる`
  - `追忆 / Remember / 思い出す` as text-only for now or omitted if too early.
  - `离开 / Leave / 離れる`

Action:

- `maidmarriage:spirit_soothe` sends `SpiritInteractionPayload` to server.
- Server validates distance and family permission.
- Server plays particles/sound and chat feedback.

## Minimal Java Package Layout

Recommended package:

`com.example.maidmarriage.client.interaction`

Files:

- `GenericMaidInteractionScreen.java`
- `InteractionSession.java`
- `InteractionTargetAdapter.java`
- `InteractionTargetRegistry.java`
- `InteractionActionRegistry.java`
- `InteractionActionContext.java`
- `SpiritInteractionTargetAdapter.java`
- `BuiltinInteractionActions.java`

Keep existing `dialogueui` and `dialoguesystem` packages unchanged except where small generalization is necessary.

## Migration Boundary

This work must not migrate `HugActionScreen` yet.

`HugActionScreen` remains responsible for:

- Adult maid hug/kiss/lap pillow UI.
- Child maid existing interaction UI.
- Current voice button behavior.
- Existing layout debug controls.

The new generic framework starts with spirits only. Once stable, adult and child interactions can be migrated later in controlled steps.

## Robustness Rules

- Unknown target type: do not open UI.
- Missing scenario: show an empty/fallback frame, log warning, do not crash.
- Unknown action: show debug text if debug enabled, do not crash.
- Target entity gone: close screen safely.
- Server validation remains authoritative for actions.
- JSON must be parseable and UTF-8.

## Implementation Order

1. Remove the temporary hardcoded `MaidSpiritInteractionScreen`.
2. Add interaction framework core classes.
3. Add spirit adapter and built-in spirit action.
4. Add localized spirit scenario JSON.
5. Route crosshair spirit target to the generic screen.
6. Build and run JSON validation.


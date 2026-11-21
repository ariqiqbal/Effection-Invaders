# Effection

## Team Introduction

Hello everyone! We’re the Effection Team!

Our team is responsible for developing the Visual Effects System for the game. 
We'll be working on several improvements and new features aimed at enhancing the overall visual experience, gameplay feedback, and immersion.

In the second project, the whole game is maintained in this repository by our team. We improve it (WBS 3.0) with a managed process: Jira (space VFX), code review, automated tests and CI. See [README.md](README.md).

## Members

| Name           | Role                        | GitHub                                          |
|----------------|-----------------------------|-------------------------------------------------|
| Elisya Natasha | Project Manager             | [deluluclover](https://github.com/deluluclover) |
| Alya Fatihah   | Scrum / Jira Master         | [alyafths](https://github.com/alyafths)         |
| NurSofia       | Risk Analyst, Developer     | [sofiajourke](https://github.com/sofiajourke)   |
| Najihah        | Estimation Lead, Developer  | [najihahs](https://github.com/najihahs)         |
| Ariq Iqbal     | Git Master                  | [ariqiqbal](https://github.com/ariqiqbal)       |
| Khairul Suffie | Quality Lead                | [KaiSuf](https://github.com/KaiSuf)             |
| Dian Melissa   | Quality Tester              | [dianpines](https://github.com/dianpines)       |
| Maisarah       | Report Lead, Developer      | [maisarah-mg](https://github.com/maisarah-mg)   |

---

## Requirements

- The system shall maintain a consistent retro/pixel-art visual style.
- The system shall optimize particle processing to minimize the impact on game performance.
- The system shall reuse VFX components where possible.
- The system shall automatically remove completed effects to prevent unnecessary resource usage.

---

## Aim & Goals

### 1. Combat & Interaction Effects

The system shall provide distinct visual feedback for combat interactions, including weapon firing, hit impacts, enemy destruction, and player death.



### 1.1 Weapon Firing

When the player or enemy fires a projectile, the system shall trigger the appropriate firing effect.

 ```text
Player/Enemy 
  │ 
  └── 🔫 Shoot 
       ↓ 
   Projectile 
       ↓ 
  Small trail effect
```

**Effects:**

- Player bullets shall leave a short visual trail.
- Enemy bullets shall have a visually distinct trail.
- A muzzle flash shall appear when firing.
- Small pixel particles shall be emitted from the weapon.

**Events:**

- `PLAYER_SHOOT`
- `ENEMY_SHOOT`



### 1.2 Hit Impact

When a projectile hits a player or enemy, the system shall display an impact effect at the hit location.

 ```text
Bullet → Enemy
           ↓
       Hit detected
           ↓
      Impact VFX
```

**Effects:**

- A small flash shall appear at the point of impact.
- 3–6 particles shall be emitted.
- A brief sprite animation shall be displayed.
- A small explosion effect shall be displayed.
- The visual effect may be synchronized with the corresponding hit sound.

**Events:**

- `PLAYER_HIT`
- `ENEMY_HIT`



### 1.3 Enemy Destruction

When an enemy is defeated, the system shall trigger an enemy destruction effect.

**Sequence:**

```text
Enemy 
  ↓ 
Flash 
  ↓ 
Expand 
  ↓ 
Particles + debris 
  ↓ 
Fade out 
  ↓ 
Remove
```

**Effects:**

- The enemy shall briefly flash when destroyed.
- The explosion shall expand during the destruction animation.
- Pixel particles and debris shall spread outward.
- The effect shall fade out after the animation.
- The completed effect shall be automatically removed.
- The destruction effect may vary according to the enemy type or level.

**Event:**

- `ENEMY_DESTROYED`



### 1.4 Player Death / Game Over

When the player is destroyed, the VFX system shall display a destruction sequence followed by the Game Over transition.

**Sequence:**

```text
Player hit 
   ↓ 
Player flashes 
   ↓ 
Ship explodes 
   ↓ 
Debris spreads outward 
   ↓ 
Screen shake 
   ↓ 
"GAME OVER" 
   ↓ 
Game Over transition

```

**Effects:**

- The player ship shall flash after being hit.
- An explosion animation shall be triggered.
- Debris shall spread outward from the destroyed ship.
- Screen shake shall be triggered during the destruction sequence.
- `"GAME OVER"` shall be displayed after the destruction effect.
- The VFX shall transition to the Game Over state.

**Events:**

- `PLAYER_DESTROYED`
- `GAME_OVER`



### 2. Environmental Effects

Enhance the game environment and level progression through particle effects and visual transitions, including:

- Floating stars.
- Tiny space particles.
- Sparks from damaged objects.
- Smoke from destroyed ships.
- Debris floating after explosions.
- Level completion effects such as `"STAGE CLEAR"`, pixel fireworks, and screen transitions.



### 3. Synchronization & Accessibility

Provide reusable VFX components and libraries that can be easily used by other game systems, including:

- Reusable VFX components.
- Consistent VFX events.
- Synchronization with game events.
- Easy integration with other game systems.
- Events such as

```text
`PLAYER_SHOOT`, `ENEMY_SHOOT`, `PLAYER_HIT`, `ENEMY_HIT`, `ENEMY_DESTROYED`, `PLAYER_DESTROYED`, `LEVEL_COMPLETED`, `GAME_OVER`, and `STAGE_STARTED`.
```

### 4. Shop Effects

Cosmetic effects players can buy in the shop's **EFFECTS** tab. Bought once, on in every run. Look only, no gameplay change.

| Effect | Price | What it does | Code |
|--------|-------|--------------|------|
| Warp In | 45 coins / 25 diamonds | The ship beams into the game at the start of each level (landing on "GO!") and, shorter, after a lost life: lock-on brackets, a rift opens, a beam strikes down, the ship rains in as a hologram, a scan line turns it solid, then an impact flash with light rays, ground rings, debris and a small screen shake. In the selected skin's colour; the shop shows a looping preview. | `src/engine/vfx/WarpIn.java` |
| Ghost Trail | 55 coins / 30 diamonds | Fading copies of the ship follow it while it moves, in the selected skin's colour. Faster ship, stronger ghosts. While the Boost item is active, the trail surges (two extra ghosts and a brighter glow). Hidden while the ship is destroyed, on pause, game over and level clear. | `src/engine/vfx/GhostTrail.java` |

---


## Integration with Other Game Systems

### 1. Player & Enemy Ships

The Visual Effect System needs access to the player and enemy entities, including their position, sprite/ship type, hitbox, and destruction state. This allows effects such as enemy hit flashes, explosions, and player-destruction effects to be attached to the correct ship, and to stay consistent with the different player and enemy variants.

### 2. Level Design

The Visual Effect System needs reliable wave/level start, wave completion, and level-transition events. These events trigger effects such as wave-clear animations, level-transition effects, and new-wave introduction effects at the correct point in the gameplay sequence.

### 3. Sound Effects / BGM

Visual and audio effects are triggered from the same gameplay events (player shooting, enemy hits, enemy destruction, player damage/death) with consistent timing, so that, for example, an enemy explosion and its explosion sound occur together.

---

## Game font

- Default: `res/silkscreen/Silkscreen-Regular.ttf` (SIL Open Font License 1.1, see `res/silkscreen/OFL.txt`)
- Also included: `Silkscreen-Bold.ttf` (try it: `-Dgame.font=silkscreen/Silkscreen-Bold.ttf`)
- Fallback: `res/font.ttf` (used if the default font is missing)
- Try another font: `java -Dgame.font=<path> -cp "out;res" engine.Core`
- Long key hint lines shrink to fit the screen width.

## Lightning Chain (kill effect)

- On enemy kill, a forked bolt jumps to the nearest alive enemies (look only).
- Streak: kills within 1.5 s. Reset on timeout or player hit (PLAYER_HIT).
- Tier 1 (1–2 kills): blue, 2 jumps. Tier 2 (3–5): violet, 3 jumps.
  Tier 3 (6+): gold, 4 jumps, bigger flash.
- Test switch until the shop sells it: `java -Dvfx.lightning=true -cp "out;res" engine.Core`
- Files: `engine/vfx/LightningChain.java`, `GameScreen`, `DrawManager.drawLightningChain`,
  `ShopScreen` (placeholder + preview).
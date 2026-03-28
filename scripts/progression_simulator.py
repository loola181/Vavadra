#!/usr/bin/env python3
"""Estimate time-to-level milestones using the same economy config as the app.

Default behaviour models a fresh player session the same way as the app now does:
- no immediate day-0 return streak claim
- no immediately-open Arcane Cache on the first second

Use --legacy-day0-claims to reproduce the older behaviour.
"""

from __future__ import annotations

import argparse
import json
import math
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional

SECONDS_PER_DAY = 24 * 60 * 60
SECONDS_PER_WEEK = 7 * SECONDS_PER_DAY


@dataclass
class UpgradeEconomy:
    starting_power: int
    base_cost: int
    cost_offset: int
    power_per_upgrade: int


@dataclass
class PrestigeEconomy:
    base_cost: int
    level_bonus_per_ritual: int
    reset_tap_power: int
    reset_auto_power: int


@dataclass
class LevelEconomy:
    essence_factor: int
    target_levels: list[int]


@dataclass
class ReturnMilestone:
    day: int
    title: str
    reward_essence: int
    reward_shards: int


@dataclass
class ReturnStreakEconomy:
    base_reward: int
    reward_per_day: int
    shard_every_days: int
    shards_per_milestone: int
    milestones: list[ReturnMilestone]


@dataclass
class CacheEconomy:
    base_cooldown_minutes: int
    cooldown_reduction_per_level: int
    min_cooldown_minutes: int
    base_reward: int
    reward_per_level: int
    reward_per_player_level: int
    shard_reward: int


@dataclass
class RelicEconomy:
    max_level: int
    base_shards_for_level: int
    shards_growth_per_level: int
    tap_multiplier_per_level: float
    auto_multiplier_per_level: float


@dataclass
class AltarTier:
    tier: int
    title: str
    required_favor: int
    reward_essence: int
    reward_shards: int


@dataclass
class AltarEconomy:
    favor_from_cache: int
    favor_from_return: int
    tiers: list[AltarTier]


@dataclass
class RetentionEconomy:
    return_streak: ReturnStreakEconomy
    cache: CacheEconomy
    relic: RelicEconomy
    altar: AltarEconomy


@dataclass
class EconomyConfig:
    tap: UpgradeEconomy
    auto: UpgradeEconomy
    prestige: PrestigeEconomy
    levels: LevelEconomy
    retention: RetentionEconomy


@dataclass
class SimState:
    essence: float
    tap_power: int
    auto_power: int
    rituals: int
    seconds: int
    tap_upgrades: int
    auto_upgrades: int
    cache_level: int
    next_cache_at: int
    cache_claims: int
    return_streak: int
    best_return_streak: int
    last_return_claim_day: int
    relic_shards: int
    relic_level: int
    altar_favor: int
    altar_claimed_tiers: set[int] = field(default_factory=set)
    altar_tier_claims: int = 0
    altar_week: int = -1
    retention_essence: float = 0.0


@dataclass
class SimResult:
    target: int
    reached: bool
    seconds: int
    tap_upgrades: int
    auto_upgrades: int
    rituals: int
    cache_claims: int
    best_return_streak: int
    relic_level: int
    relic_shards: int
    altar_tier_claims: int
    retention_essence: int


@dataclass
class SimulatorOptions:
    clicks_per_second: float
    max_seconds: int
    allow_prestige: bool
    enable_retention: bool
    cache_level: int
    relic_level: int
    relic_shards: int
    legacy_day0_claims: bool


def load_config(path: Path) -> EconomyConfig:
    raw = json.loads(path.read_text(encoding="utf-8"))
    tap = raw["tap"]
    auto = raw["auto"]
    prestige = raw["prestige"]
    levels = raw["levels"]
    retention_raw = raw.get("retention", {})
    return_streak_raw = retention_raw.get("returnStreak", {})
    cache_raw = retention_raw.get("cache", {})
    relic_raw = retention_raw.get("relic", {})
    altar_raw = retention_raw.get("altar", {})

    return EconomyConfig(
        tap=UpgradeEconomy(
            starting_power=int(tap["startingPower"]),
            base_cost=int(tap["baseCost"]),
            cost_offset=int(tap["costOffset"]),
            power_per_upgrade=int(tap["powerPerUpgrade"]),
        ),
        auto=UpgradeEconomy(
            starting_power=int(auto["startingPower"]),
            base_cost=int(auto["baseCost"]),
            cost_offset=int(auto["costOffset"]),
            power_per_upgrade=int(auto["powerPerUpgrade"]),
        ),
        prestige=PrestigeEconomy(
            base_cost=int(prestige["baseCost"]),
            level_bonus_per_ritual=int(prestige["levelBonusPerRitual"]),
            reset_tap_power=int(prestige["resetTapPower"]),
            reset_auto_power=int(prestige["resetAutoPower"]),
        ),
        levels=LevelEconomy(
            essence_factor=int(levels["essenceFactor"]),
            target_levels=[int(level) for level in levels.get("targetLevels", [10, 25, 50, 75, 100])],
        ),
        retention=RetentionEconomy(
            return_streak=ReturnStreakEconomy(
                base_reward=int(return_streak_raw.get("baseReward", 0)),
                reward_per_day=int(return_streak_raw.get("rewardPerDay", 0)),
                shard_every_days=int(return_streak_raw.get("shardEveryDays", 0)),
                shards_per_milestone=int(return_streak_raw.get("shardsPerMilestone", 0)),
                milestones=[
                    ReturnMilestone(
                        day=int(item["day"]),
                        title=str(item.get("title", f"День {item['day']}")),
                        reward_essence=int(item.get("rewardEssence", 0)),
                        reward_shards=int(item.get("rewardShards", 0)),
                    )
                    for item in return_streak_raw.get("milestones", [])
                ],
            ),
            cache=CacheEconomy(
                base_cooldown_minutes=int(cache_raw.get("baseCooldownMinutes", 60)),
                cooldown_reduction_per_level=int(cache_raw.get("cooldownReductionPerLevel", 0)),
                min_cooldown_minutes=int(cache_raw.get("minCooldownMinutes", 10)),
                base_reward=int(cache_raw.get("baseReward", 0)),
                reward_per_level=int(cache_raw.get("rewardPerLevel", 0)),
                reward_per_player_level=int(cache_raw.get("rewardPerPlayerLevel", 0)),
                shard_reward=int(cache_raw.get("shardReward", 0)),
            ),
            relic=RelicEconomy(
                max_level=int(relic_raw.get("maxLevel", 0)),
                base_shards_for_level=int(relic_raw.get("baseShardsForLevel", 1)),
                shards_growth_per_level=int(relic_raw.get("shardsGrowthPerLevel", 1)),
                tap_multiplier_per_level=float(relic_raw.get("tapMultiplierPerLevel", 0.0)),
                auto_multiplier_per_level=float(relic_raw.get("autoMultiplierPerLevel", 0.0)),
            ),
            altar=AltarEconomy(
                favor_from_cache=int(altar_raw.get("favorFromCache", 0)),
                favor_from_return=int(altar_raw.get("favorFromReturn", 0)),
                tiers=[
                    AltarTier(
                        tier=int(item["tier"]),
                        title=str(item.get("title", f"Порог {item['tier']}")),
                        required_favor=int(item.get("requiredFavor", 0)),
                        reward_essence=int(item.get("rewardEssence", 0)),
                        reward_shards=int(item.get("rewardShards", 0)),
                    )
                    for item in altar_raw.get("tiers", [])
                ],
            ),
        ),
    )


def level_for_state(state: SimState, cfg: EconomyConfig) -> int:
    ritual_bonus = state.rituals * cfg.prestige.level_bonus_per_ritual
    essence_part = int(math.sqrt(max(0.0, state.essence) / cfg.levels.essence_factor))
    return 1 + ritual_bonus + essence_part


def essence_for_level(target_level: int, rituals: int, cfg: EconomyConfig) -> int:
    ritual_bonus = rituals * cfg.prestige.level_bonus_per_ritual
    effective = max(0, target_level - 1 - ritual_bonus)
    return effective * effective * cfg.levels.essence_factor


def upgrade_cost(base_cost: int, current_power: int, offset: int) -> int:
    factor = max(1, current_power + offset)
    return base_cost * factor * factor


def should_ritual(state: SimState, target_level: int, cfg: EconomyConfig) -> bool:
    if target_level <= 10:
        return False
    if level_for_state(state, cfg) < 10:
        return False
    if state.rituals >= 8:
        return False

    remaining_without_ritual = max(0.0, essence_for_level(target_level, state.rituals, cfg) - state.essence)
    if remaining_without_ritual <= 0:
        return False

    with_ritual = float(essence_for_level(target_level, state.rituals + 1, cfg))
    return with_ritual < remaining_without_ritual * 0.75


def relic_shards_needed(level: int, cfg: EconomyConfig) -> int:
    relic = cfg.retention.relic
    if level >= relic.max_level:
        return 0
    return max(1, relic.base_shards_for_level + level * relic.shards_growth_per_level)


def relic_tap_multiplier(level: int, cfg: EconomyConfig) -> float:
    return max(1.0, 1.0 + level * cfg.retention.relic.tap_multiplier_per_level)


def relic_auto_multiplier(level: int, cfg: EconomyConfig) -> float:
    return max(1.0, 1.0 + level * cfg.retention.relic.auto_multiplier_per_level)


def cache_cooldown_seconds(level: int, cfg: EconomyConfig) -> int:
    cache = cfg.retention.cache
    minutes = max(
        cache.min_cooldown_minutes,
        cache.base_cooldown_minutes - level * cache.cooldown_reduction_per_level,
    )
    return minutes * 60


def cache_reward(level: int, player_level: int, cfg: EconomyConfig) -> int:
    cache = cfg.retention.cache
    return max(
        cache.base_reward,
        cache.base_reward + level * cache.reward_per_level + player_level * cache.reward_per_player_level,
    )


def return_reward_for_day(day: int, cfg: EconomyConfig) -> int:
    streak = cfg.retention.return_streak
    return max(streak.base_reward, streak.base_reward + max(day - 1, 0) * streak.reward_per_day)


def return_milestone_for_day(day: int, cfg: EconomyConfig) -> Optional[ReturnMilestone]:
    for milestone in cfg.retention.return_streak.milestones:
        if milestone.day == day:
            return milestone
    return None


def choose_upgrade(state: SimState, cfg: EconomyConfig, clicks_per_second: float) -> Optional[str]:
    candidates: list[tuple[str, float, int]] = []

    tap_cost = upgrade_cost(cfg.tap.base_cost, state.tap_power, cfg.tap.cost_offset)
    tap_gain = clicks_per_second * cfg.tap.power_per_upgrade * relic_tap_multiplier(state.relic_level, cfg)
    if state.essence >= tap_cost and tap_gain > 0:
        candidates.append(("tap", tap_cost / tap_gain, tap_cost))

    auto_cost = upgrade_cost(cfg.auto.base_cost, state.auto_power, cfg.auto.cost_offset)
    auto_gain = cfg.auto.power_per_upgrade * relic_auto_multiplier(state.relic_level, cfg)
    if state.essence >= auto_cost and auto_gain > 0:
        candidates.append(("auto", auto_cost / auto_gain, auto_cost))

    if not candidates:
        return None

    candidates.sort(key=lambda item: (item[1], item[2]))
    return candidates[0][0]


def apply_relic_progress(state: SimState, cfg: EconomyConfig) -> None:
    while state.relic_level < cfg.retention.relic.max_level:
        needed = relic_shards_needed(state.relic_level, cfg)
        if needed <= 0 or state.relic_shards < needed:
            return
        state.relic_shards -= needed
        state.relic_level += 1


def claim_altar_tiers(state: SimState, cfg: EconomyConfig) -> None:
    for tier in sorted(cfg.retention.altar.tiers, key=lambda item: item.required_favor):
        if tier.tier in state.altar_claimed_tiers:
            continue
        if state.altar_favor < tier.required_favor:
            continue
        state.altar_claimed_tiers.add(tier.tier)
        state.altar_tier_claims += 1
        state.essence += tier.reward_essence
        state.retention_essence += tier.reward_essence
        if tier.reward_shards > 0:
            state.relic_shards += tier.reward_shards
            apply_relic_progress(state, cfg)


def add_altar_favor(state: SimState, amount: int, cfg: EconomyConfig) -> None:
    if amount <= 0:
        return
    state.altar_favor += amount
    claim_altar_tiers(state, cfg)


def maybe_reset_week(state: SimState) -> None:
    current_week = state.seconds // SECONDS_PER_WEEK
    if current_week == state.altar_week:
        return
    state.altar_week = current_week
    state.altar_favor = 0
    state.altar_claimed_tiers.clear()


def maybe_claim_cache(state: SimState, cfg: EconomyConfig) -> None:
    while state.seconds >= state.next_cache_at:
        reward = cache_reward(state.cache_level, level_for_state(state, cfg), cfg)
        state.essence += reward
        state.retention_essence += reward
        state.cache_claims += 1
        if cfg.retention.cache.shard_reward > 0:
            state.relic_shards += cfg.retention.cache.shard_reward
            apply_relic_progress(state, cfg)
        add_altar_favor(state, cfg.retention.altar.favor_from_cache, cfg)
        state.next_cache_at = state.seconds + cache_cooldown_seconds(state.cache_level, cfg)


def maybe_claim_return_streak(state: SimState, cfg: EconomyConfig) -> None:
    current_day = state.seconds // SECONDS_PER_DAY
    if current_day == state.last_return_claim_day:
        return

    if state.last_return_claim_day == current_day - 1:
        next_streak = state.return_streak + 1
    else:
        next_streak = 1

    reward = return_reward_for_day(next_streak, cfg)
    shard_reward = 0
    streak_cfg = cfg.retention.return_streak
    if streak_cfg.shard_every_days > 0 and next_streak % streak_cfg.shard_every_days == 0:
        shard_reward += streak_cfg.shards_per_milestone

    milestone = return_milestone_for_day(next_streak, cfg)
    if milestone is not None:
        reward += milestone.reward_essence
        shard_reward += milestone.reward_shards

    state.essence += reward
    state.retention_essence += reward
    state.return_streak = next_streak
    state.best_return_streak = max(state.best_return_streak, next_streak)
    state.last_return_claim_day = current_day

    if shard_reward > 0:
        state.relic_shards += shard_reward
        apply_relic_progress(state, cfg)

    add_altar_favor(state, cfg.retention.altar.favor_from_return, cfg)


def tick_retention(state: SimState, cfg: EconomyConfig, enabled: bool) -> None:
    if not enabled:
        return
    maybe_reset_week(state)
    maybe_claim_cache(state, cfg)
    maybe_claim_return_streak(state, cfg)


def build_initial_state(cfg: EconomyConfig, options: SimulatorOptions) -> SimState:
    cache_start = 0 if options.legacy_day0_claims else cache_cooldown_seconds(options.cache_level, cfg)
    return_claim_day = -1 if options.legacy_day0_claims else 0
    return SimState(
        essence=0.0,
        tap_power=cfg.tap.starting_power,
        auto_power=cfg.auto.starting_power,
        rituals=0,
        seconds=0,
        tap_upgrades=0,
        auto_upgrades=0,
        cache_level=max(0, options.cache_level),
        next_cache_at=cache_start,
        cache_claims=0,
        return_streak=0,
        best_return_streak=0,
        last_return_claim_day=return_claim_day,
        relic_shards=max(0, options.relic_shards),
        relic_level=max(0, options.relic_level),
        altar_favor=0,
        altar_week=0,
    )


def simulate_target(target_level: int, cfg: EconomyConfig, options: SimulatorOptions) -> SimResult:
    state = build_initial_state(cfg, options)
    apply_relic_progress(state, cfg)

    while state.seconds < options.max_seconds:
        tick_retention(state, cfg, options.enable_retention)

        if level_for_state(state, cfg) >= target_level:
            return SimResult(
                target=target_level,
                reached=True,
                seconds=state.seconds,
                tap_upgrades=state.tap_upgrades,
                auto_upgrades=state.auto_upgrades,
                rituals=state.rituals,
                cache_claims=state.cache_claims,
                best_return_streak=state.best_return_streak,
                relic_level=state.relic_level,
                relic_shards=state.relic_shards,
                altar_tier_claims=state.altar_tier_claims,
                retention_essence=int(round(state.retention_essence)),
            )

        state.essence += state.auto_power * relic_auto_multiplier(state.relic_level, cfg)
        state.essence += state.tap_power * options.clicks_per_second * relic_tap_multiplier(state.relic_level, cfg)

        while True:
            decision = choose_upgrade(state, cfg, options.clicks_per_second)
            if decision is None:
                break
            if decision == "tap":
                cost = upgrade_cost(cfg.tap.base_cost, state.tap_power, cfg.tap.cost_offset)
                if state.essence < cost:
                    break
                state.essence -= cost
                state.tap_power += cfg.tap.power_per_upgrade
                state.tap_upgrades += 1
            else:
                cost = upgrade_cost(cfg.auto.base_cost, state.auto_power, cfg.auto.cost_offset)
                if state.essence < cost:
                    break
                state.essence -= cost
                state.auto_power += cfg.auto.power_per_upgrade
                state.auto_upgrades += 1

        ritual_cost = cfg.prestige.base_cost * (state.rituals + 1)
        if options.allow_prestige and state.essence >= ritual_cost and should_ritual(state, target_level, cfg):
            state.essence = 0.0
            state.tap_power = cfg.prestige.reset_tap_power
            state.auto_power = cfg.prestige.reset_auto_power
            state.rituals += 1

        state.seconds += 1

    return SimResult(
        target=target_level,
        reached=False,
        seconds=options.max_seconds,
        tap_upgrades=state.tap_upgrades,
        auto_upgrades=state.auto_upgrades,
        rituals=state.rituals,
        cache_claims=state.cache_claims,
        best_return_streak=state.best_return_streak,
        relic_level=state.relic_level,
        relic_shards=state.relic_shards,
        altar_tier_claims=state.altar_tier_claims,
        retention_essence=int(round(state.retention_essence)),
    )


def run_targets(cfg: EconomyConfig, targets: list[int], options: SimulatorOptions) -> list[SimResult]:
    return [simulate_target(target, cfg, options) for target in targets]


def format_duration(total_seconds: int) -> str:
    hours, remainder = divmod(total_seconds, 3600)
    minutes, seconds = divmod(remainder, 60)
    return f"{hours:02d}:{minutes:02d}:{seconds:02d}"


def format_delta(seconds: int) -> str:
    sign = "+" if seconds >= 0 else "-"
    safe = abs(seconds)
    return f"{sign}{format_duration(safe)}"


def parse_targets(raw: str, defaults: list[int]) -> list[int]:
    if not raw.strip():
        return defaults
    return sorted({int(item.strip()) for item in raw.split(",") if item.strip()})


def infer_label(path: Path, fallback: str) -> str:
    stem = path.stem.strip()
    return stem or fallback


def print_results(label: str, results: list[SimResult], max_seconds: int) -> None:
    print(f"[{label}]")
    for result in results:
        if result.reached:
            print(
                f"L{result.target:>3}: {format_duration(result.seconds)} | "
                f"tap+ {result.tap_upgrades}, auto+ {result.auto_upgrades}, rituals {result.rituals} | "
                f"cache {result.cache_claims}, streak {result.best_return_streak}, relic {result.relic_level}, altar {result.altar_tier_claims} | "
                f"цикл +{result.retention_essence}"
            )
        else:
            print(
                f"L{result.target:>3}: not reached within {format_duration(max_seconds)} | "
                f"tap+ {result.tap_upgrades}, auto+ {result.auto_upgrades}, rituals {result.rituals} | "
                f"cache {result.cache_claims}, streak {result.best_return_streak}, relic {result.relic_level}, altar {result.altar_tier_claims} | "
                f"цикл +{result.retention_essence}"
            )


def print_comparison(label_a: str, results_a: list[SimResult], label_b: str, results_b: list[SimResult]) -> None:
    print("-")
    print(f"compare: {label_a} -> {label_b}")
    for left, right in zip(results_a, results_b):
        if left.reached and right.reached:
            delta_seconds = right.seconds - left.seconds
            delta_retention = right.retention_essence - left.retention_essence
            print(
                f"L{left.target:>3}: {format_duration(left.seconds)} -> {format_duration(right.seconds)} "
                f"({format_delta(delta_seconds)}) | цикл {left.retention_essence} -> {right.retention_essence} "
                f"({delta_retention:+d})"
            )
        else:
            print(
                f"L{left.target:>3}: reached={left.reached} -> reached={right.reached} | "
                f"цикл {left.retention_essence} -> {right.retention_essence}"
            )


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    default_config = root / "app" / "src" / "main" / "assets" / "economy_config.json"

    parser = argparse.ArgumentParser(description="Simulate progression to selected levels.")
    parser.add_argument("--config", type=Path, default=default_config, help="Path to economy config JSON")
    parser.add_argument("--compare-config", type=Path, default=None, help="Optional second config to compare against")
    parser.add_argument("--label", type=str, default="", help="Optional label for --config output")
    parser.add_argument("--compare-label", type=str, default="", help="Optional label for --compare-config output")
    parser.add_argument("--clicks-per-second", type=float, default=4.0, help="Average manual clicks per second")
    parser.add_argument("--max-seconds", type=int, default=72 * 3600, help="Simulation timeout for each target")
    parser.add_argument("--targets", type=str, default="", help="Comma-separated levels (example: 10,25,50)")
    parser.add_argument("--no-prestige", action="store_true", help="Disable ritual usage in simulation")
    parser.add_argument("--no-retention", action="store_true", help="Ignore cache / streak / relic / altar loop")
    parser.add_argument("--cache-level", type=int, default=0, help="Fixed cache level used by the retention model")
    parser.add_argument("--relic-level", type=int, default=0, help="Starting relic level")
    parser.add_argument("--relic-shards", type=int, default=0, help="Starting relic shards")
    parser.add_argument("--legacy-day0-claims", action="store_true", help="Reproduce old immediate day-0 cache and return claims")
    args = parser.parse_args()

    cfg = load_config(args.config)
    targets = parse_targets(args.targets, cfg.levels.target_levels)
    options = SimulatorOptions(
        clicks_per_second=args.clicks_per_second,
        max_seconds=args.max_seconds,
        allow_prestige=not args.no_prestige,
        enable_retention=not args.no_retention,
        cache_level=args.cache_level,
        relic_level=args.relic_level,
        relic_shards=args.relic_shards,
        legacy_day0_claims=args.legacy_day0_claims,
    )

    print("Vavarda progression simulator")
    print(f"config: {args.config}")
    print(f"clicks/sec: {options.clicks_per_second}")
    print(f"prestige: {'off' if not options.allow_prestige else 'on'}")
    if options.enable_retention:
        print(
            "retention: on "
            f"(cache lvl {options.cache_level}, relic lvl {options.relic_level}, shards {options.relic_shards})"
        )
        print(f"startup: {'legacy day-0 claims' if options.legacy_day0_claims else 'fresh start'}")
        print("assumption: altar feeds only from cache and return streak; boss/dailies are not simulated")
    else:
        print("retention: off")
    print("-")

    label = args.label or infer_label(args.config, "current")
    results = run_targets(cfg, targets, options)
    print_results(label, results, options.max_seconds)

    if args.compare_config is not None:
        compare_cfg = load_config(args.compare_config)
        compare_label = args.compare_label or infer_label(args.compare_config, "compare")
        compare_results = run_targets(compare_cfg, targets, options)
        print_comparison(compare_label, compare_results, label, results)


if __name__ == "__main__":
    main()

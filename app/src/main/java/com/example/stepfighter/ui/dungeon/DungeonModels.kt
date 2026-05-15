package com.example.stepfighter.ui.dungeon

import androidx.annotation.StringRes
import com.example.stepfighter.R

data class EnemyData(
    @StringRes val nameRes: Int,
    val imageRes: Int,
    val maxHp: Int,
    val power: Int,
    val costToHit: Int
)

data class DungeonLevel(
    val id: Int,
    @StringRes val nameRes: Int,
    val enemies: List<EnemyData>
)

val dungeonLevelsData = listOf(

    DungeonLevel(1, R.string.dungeon_1_name, listOf(
        EnemyData(R.string.enemy_rat, R.drawable.enemy_placeholder, 40, 5, 8),
        EnemyData(R.string.enemy_goblin_scout, R.drawable.enemy_placeholder, 60, 8, 12),
        EnemyData(R.string.enemy_bat, R.drawable.enemy_placeholder, 30, 4, 5),
        EnemyData(R.string.enemy_slime_small, R.drawable.enemy_placeholder, 50, 6, 10),
        EnemyData(R.string.enemy_boss_goblin, R.drawable.enemy_placeholder, 300, 20, 30)
    )),
    DungeonLevel(2, R.string.dungeon_2_name, listOf(
        EnemyData(R.string.enemy_spider, R.drawable.enemy_placeholder, 70, 10, 15),
        EnemyData(R.string.enemy_skeleton, R.drawable.enemy_placeholder, 90, 12, 18),
        EnemyData(R.string.enemy_zombie, R.drawable.enemy_placeholder, 120, 10, 20),
        EnemyData(R.string.enemy_ghost, R.drawable.enemy_placeholder, 50, 25, 25),
        EnemyData(R.string.enemy_boss_necromancer, R.drawable.enemy_placeholder, 450, 35, 40)
    )),
    DungeonLevel(3, R.string.dungeon_3_name, listOf(
        EnemyData(R.string.enemy_wolf, R.drawable.enemy_placeholder, 100, 15, 20),
        EnemyData(R.string.enemy_bear, R.drawable.enemy_placeholder, 200, 25, 30),
        EnemyData(R.string.enemy_boar, R.drawable.enemy_placeholder, 150, 18, 22),
        EnemyData(R.string.enemy_eagle, R.drawable.enemy_placeholder, 80, 20, 15),
        EnemyData(R.string.enemy_boss_ursine, R.drawable.enemy_placeholder, 600, 45, 50)
    )),
    DungeonLevel(4, R.string.dungeon_4_name, listOf(
        EnemyData(R.string.enemy_bandit, R.drawable.enemy_placeholder, 130, 18, 20),
        EnemyData(R.string.enemy_thief, R.drawable.enemy_placeholder, 100, 22, 15),
        EnemyData(R.string.enemy_mercenary, R.drawable.enemy_placeholder, 180, 25, 25),
        EnemyData(R.string.enemy_assassin, R.drawable.enemy_placeholder, 110, 40, 30),
        EnemyData(R.string.enemy_boss_bandit_leader, R.drawable.enemy_placeholder, 800, 55, 60)
    )),
    DungeonLevel(5, R.string.dungeon_5_name, listOf(
        EnemyData(R.string.enemy_imp, R.drawable.enemy_placeholder, 150, 25, 25),
        EnemyData(R.string.enemy_succubus, R.drawable.enemy_placeholder, 200, 30, 35),
        EnemyData(R.string.enemy_hellhound, R.drawable.enemy_placeholder, 250, 40, 40),
        EnemyData(R.string.enemy_demon_warrior, R.drawable.enemy_placeholder, 400, 50, 50),
        EnemyData(R.string.enemy_boss_pitlord, R.drawable.enemy_placeholder, 1200, 80, 100)
    )),


    DungeonLevel(6, R.string.dungeon_6_name, listOf(
        EnemyData(R.string.enemy_scorpion, R.drawable.enemy_placeholder, 180, 28, 25),
        EnemyData(R.string.enemy_mummy, R.drawable.enemy_placeholder, 300, 20, 30),
        EnemyData(R.string.enemy_sand_worm, R.drawable.enemy_placeholder, 250, 35, 35),
        EnemyData(R.string.enemy_anubis_guard, R.drawable.enemy_placeholder, 350, 45, 45),
        EnemyData(R.string.enemy_boss_pharaoh, R.drawable.enemy_placeholder, 1500, 90, 120)
    )),
    DungeonLevel(7, R.string.dungeon_7_name, listOf(
        EnemyData(R.string.enemy_harpy, R.drawable.enemy_placeholder, 200, 40, 30),
        EnemyData(R.string.enemy_cyclops, R.drawable.enemy_placeholder, 800, 60, 70),
        EnemyData(R.string.enemy_minotaur, R.drawable.enemy_placeholder, 600, 55, 60),
        EnemyData(R.string.enemy_medusa, R.drawable.enemy_placeholder, 400, 70, 50),
        EnemyData(R.string.enemy_boss_chimera, R.drawable.enemy_placeholder, 2000, 100, 150)
    )),
    DungeonLevel(8, R.string.dungeon_8_name, listOf(
        EnemyData(R.string.enemy_pirate, R.drawable.enemy_placeholder, 250, 35, 30),
        EnemyData(R.string.enemy_siren, R.drawable.enemy_placeholder, 220, 50, 35),
        EnemyData(R.string.enemy_crab_giant, R.drawable.enemy_placeholder, 500, 40, 50),
        EnemyData(R.string.enemy_kraken_tentacle, R.drawable.enemy_placeholder, 400, 60, 60),
        EnemyData(R.string.enemy_boss_kraken, R.drawable.enemy_placeholder, 2500, 120, 200)
    )),
    DungeonLevel(9, R.string.dungeon_9_name, listOf(
        EnemyData(R.string.enemy_cultist, R.drawable.enemy_placeholder, 300, 45, 40),
        EnemyData(R.string.enemy_dark_priest, R.drawable.enemy_placeholder, 280, 60, 45),
        EnemyData(R.string.enemy_shadow_beast, R.drawable.enemy_placeholder, 500, 70, 60),
        EnemyData(R.string.enemy_void_walker, R.drawable.enemy_placeholder, 450, 85, 70),
        EnemyData(R.string.enemy_boss_cthulhu_spawn, R.drawable.enemy_placeholder, 3000, 150, 250)
    )),
    DungeonLevel(10, R.string.dungeon_10_name, listOf(
        EnemyData(R.string.enemy_lava_slime, R.drawable.enemy_placeholder, 400, 50, 50),
        EnemyData(R.string.enemy_fire_elemental, R.drawable.enemy_placeholder, 600, 80, 70),
        EnemyData(R.string.enemy_ifrit, R.drawable.enemy_placeholder, 550, 95, 80),
        EnemyData(R.string.enemy_phoenix, R.drawable.enemy_placeholder, 800, 110, 100),
        EnemyData(R.string.enemy_boss_fire_dragon, R.drawable.enemy_placeholder, 5000, 200, 400)
    )),


    DungeonLevel(11, R.string.dungeon_11_name, listOf(
        EnemyData(R.string.enemy_ice_wolf, R.drawable.enemy_placeholder, 500, 60, 60),
        EnemyData(R.string.enemy_yeti, R.drawable.enemy_placeholder, 1000, 90, 100),
        EnemyData(R.string.enemy_frost_giant, R.drawable.enemy_placeholder, 1500, 120, 150),
        EnemyData(R.string.enemy_boss_lich_king, R.drawable.enemy_placeholder, 7000, 250, 500)
    )),
    DungeonLevel(12, R.string.dungeon_12_name, listOf(
        EnemyData(R.string.enemy_vampire_bat, R.drawable.enemy_placeholder, 400, 70, 50),
        EnemyData(R.string.enemy_vampire_fledgeling, R.drawable.enemy_placeholder, 600, 90, 80),
        EnemyData(R.string.enemy_vampire_lord, R.drawable.enemy_placeholder, 1200, 150, 150),
        EnemyData(R.string.enemy_boss_dracula, R.drawable.enemy_placeholder, 8000, 300, 600)
    )),

    *(13..29).map { id ->
        DungeonLevel(id, R.string.generic_dungeon_name, listOf(
            EnemyData(R.string.generic_enemy, R.drawable.enemy_placeholder, 1000 + (id * 100), 100 + (id * 5), 100 + id),
            EnemyData(R.string.generic_enemy_elite, R.drawable.enemy_placeholder, 2000 + (id * 150), 150 + (id * 8), 150 + id),
            EnemyData(R.string.generic_boss, R.drawable.enemy_placeholder, 10000 + (id * 500), 400 + (id * 10), 800 + id)
        ))
    }.toTypedArray(),


    DungeonLevel(30, R.string.dungeon_30_name, listOf(
        EnemyData(R.string.enemy_final_guard_1, R.drawable.enemy_placeholder, 5000, 200, 200),
        EnemyData(R.string.enemy_final_guard_2, R.drawable.enemy_placeholder, 6000, 250, 250),
        EnemyData(R.string.enemy_final_guard_3, R.drawable.enemy_placeholder, 7000, 300, 300),
        EnemyData(R.string.enemy_boss_god_of_steps, R.drawable.enemy_placeholder, 50000, 1000, 2000)
    ))
)
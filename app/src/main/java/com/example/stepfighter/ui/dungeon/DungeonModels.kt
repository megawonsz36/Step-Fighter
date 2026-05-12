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
    DungeonLevel(
        id = 1,
        nameRes = R.string.dungeon_1_name,
        enemies = listOf(
            EnemyData(R.string.enemy_goblin_scout, R.drawable.enemy_placeholder, 50, 5, 10),
            EnemyData(R.string.enemy_rat, R.drawable.enemy_placeholder, 40, 8, 8),
            EnemyData(R.string.enemy_skeleton, R.drawable.enemy_placeholder, 80, 12, 15),
            EnemyData(R.string.enemy_boss_goblin, R.drawable.enemy_placeholder, 250, 30, 25)
        )
    ),
    DungeonLevel(
        id = 2,
        nameRes = R.string.dungeon_2_name,
        enemies = listOf(
            EnemyData(R.string.enemy_wolf_young, R.drawable.enemy_placeholder, 70, 15, 12),
            EnemyData(R.string.enemy_spider, R.drawable.enemy_placeholder, 60, 18, 10),
            EnemyData(R.string.enemy_wolf, R.drawable.enemy_placeholder, 110, 22, 18),
            EnemyData(R.string.enemy_alpha, R.drawable.enemy_placeholder, 400, 45, 40)
        )
    ),
    DungeonLevel(
        id = 3,
        nameRes = R.string.dungeon_3_name,
        enemies = listOf(
            EnemyData(R.string.enemy_slime, R.drawable.enemy_placeholder, 90, 20, 15),
            EnemyData(R.string.enemy_drowner, R.drawable.enemy_placeholder, 100, 25, 18),
            EnemyData(R.string.enemy_toad, R.drawable.enemy_placeholder, 130, 30, 22),
            EnemyData(R.string.enemy_hag, R.drawable.enemy_placeholder, 600, 60, 50)
        )
    ),
    DungeonLevel(
        id = 4,
        nameRes = R.string.dungeon_4_name,
        enemies = listOf(
            EnemyData(R.string.enemy_troll, R.drawable.enemy_placeholder, 200, 40, 30),
            EnemyData(R.string.enemy_golem, R.drawable.enemy_placeholder, 900, 80, 70)
        )
    )
)
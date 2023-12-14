package ru.mtuci.bbca.scale

import android.content.res.Resources
import ru.mtuci.bbca.R
import ru.mtuci.bbca.scale.overlay_image_view.OverlayItem

object CharactersProvider {
    fun provide(resources: Resources) = listOf(
        OverlayItem(
            id = "wally",
            x = 2098,
            y = 484,
            width = 122,
            height = 181,
            name = resources.getString(R.string.character_wally)
        ),
        OverlayItem(
            id = "batman",
            x = 442,
            y = 1839,
            width = 136,
            height = 223,
            name = resources.getString(R.string.character_batman)
        ),
        OverlayItem(
            id = "robin",
            x = 730,
            y = 1812,
            width = 144,
            height = 211,
            name = resources.getString(R.string.character_robin)
        ),
        OverlayItem(
            id = "subzero",
            x = 1345,
            y = 17,
            width = 163,
            height = 231,
            name = resources.getString(R.string.character_subzero)
        ),
        OverlayItem(
            id = "scorpion",
            x = 1571,
            y = 126,
            width = 162,
            height = 240,
            name = resources.getString(R.string.character_scorpion)
        ),
        OverlayItem(
            id = "stormtroopers",
            x = 2009,
            y = 1692,
            width = 290,
            height = 281,
            name = resources.getString(R.string.character_stormtroopers)
        ),
        OverlayItem(
            id = "finn",
            x = 295,
            y = 2337,
            width = 147,
            height = 207,
            name = resources.getString(R.string.character_finn)
        ),
        OverlayItem(
            id = "harry",
            x = 1362,
            y = 2337,
            width = 162,
            height = 207,
            name = resources.getString(R.string.character_harry)
        ),
        OverlayItem(
            id = "megabrain",
            x = 760,
            y = 519,
            width = 154,
            height = 217,
            name = resources.getString(R.string.character_megabrain)
        ),
        OverlayItem(
            id = "captain_america",
            x = 190,
            y = 1384,
            width = 144,
            height = 183,
            name = resources.getString(R.string.character_captain_america)
        ),
        OverlayItem(
            id = "waltboy",
            x = 273,
            y = 573,
            width = 146,
            height = 299,
            name = resources.getString(R.string.character_waltboy)
        ),
        OverlayItem(
            id = "spider_man",
            x = 2716,
            y = 394,
            width = 143,
            height = 180,
            name = resources.getString(R.string.character_spider_man)
        ),
        OverlayItem(
            id = "super_family",
            x = 3280,
            y = 653,
            width = 198,
            height = 206,
            name = resources.getString(R.string.character_super_family)
        ),
        OverlayItem(
            id = "deadpool",
            x = 2649,
            y = 1812,
            width = 133,
            height = 193,
            name = resources.getString(R.string.character_deadpool)
        ),
        OverlayItem(
            id = "chief",
            x = 1912,
            y = 297,
            width = 153,
            height = 222,
            name = resources.getString(R.string.character_chief)
        ),
        OverlayItem(
            id = "zoidberg",
            x = 419,
            y = 1011,
            width = 124,
            height = 171,
            name = resources.getString(R.string.character_zoidberg)
        ),
        OverlayItem(
            id = "hulk",
            x = 975,
            y = 1491,
            width = 205,
            height = 276,
            name = resources.getString(R.string.character_hulk)
        ),
        OverlayItem(
            id = "joker",
            x = 885,
            y = 444,
            width = 146,
            height = 229,
            name = resources.getString(R.string.character_joker)
        ),
        OverlayItem(
            id = "leonid",
            x = 885,
            y = 1779,
            width = 162,
            height = 194,
            name = resources.getString(R.string.character_leonid)
        ),
        OverlayItem(
            id = "flash",
            x = 1483,
            y = 1458,
            width = 130,
            height = 194,
            name = resources.getString(R.string.character_flash)
        ),
    )
}
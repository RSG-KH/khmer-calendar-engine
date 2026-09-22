@file:OptIn(kotlin.js.ExperimentalJsExport::class)

package com.rsgkh.calendar.engine

import kotlin.js.JsExport

@JsExport
enum class HeavenlyStem(
    val index: Int,
    val chineseName: String,
    val pinyin: String,
    val element: String
) {
    JIA(0, "甲", "Jiǎ", "Yang Wood"),
    YI(1, "乙", "Yǐ", "Yin Wood"),
    BING(2, "丙", "Bǐng", "Yang Fire"),
    DING(3, "丁", "Dīng", "Yin Fire"),
    WU(4, "戊", "Wù", "Yang Earth"),
    JI(5, "己", "Jǐ", "Yin Earth"),
    GENG(6, "庚", "Gēng", "Yang Metal"),
    XIN(7, "辛", "Xīn", "Yin Metal"),
    REN(8, "壬", "Rén", "Yang Water"),
    GUI(9, "癸", "Guǐ", "Yin Water");

    companion object {
        fun fromIndex(index: Int): HeavenlyStem = entries[floorMod(index, 10)]
    }
}

@JsExport
enum class EarthlyBranch(
    val index: Int,
    val chineseName: String,
    val pinyin: String,
    val animal: String,
    val khmerAnimal: String
) {
    ZI(0, "子", "Zǐ", "Rat", "ជូត (Chuot)"),
    CHOU(1, "丑", "Chǒu", "Ox", "ឆ្លូវ (Chhlov)"),
    YIN(2, "寅", "Yín", "Tiger", "ខាល (Khal)"),
    MAO(3, "卯", "Mǎo", "Rabbit", "ថោះ (Thoh)"),
    CHEN(4, "辰", "Chén", "Dragon", "រោង (Rong)"),
    SI(5, "巳", "Sì", "Snake", "ម្សាញ់ (Maseng)"),
    WU(6, "午", "Wǔ", "Horse", "មមី (Momee)"),
    WEI(7, "未", "Wèi", "Goat", "មមែ (Momae)"),
    SHEN(8, "申", "Shēn", "Monkey", "វក (Vok)"),
    YOU(9, "酉", "Yǒu", "Rooster", "រកា (Roka)"),
    XU(10, "戌", "Xū", "Dog", "ច (Cho)"),
    HAI(11, "亥", "Hài", "Pig", "កុរ (Kor)");

    companion object {
        fun fromIndex(index: Int): EarthlyBranch = entries[floorMod(index, 12)]
    }
}

@JsExport
data class GanzhiPillar(
    val stem: HeavenlyStem,
    val branch: EarthlyBranch
) {
    init {
        freezeValue(this)
    }

    val nameZh: String get() = "${stem.chineseName}${branch.chineseName}"
    val pinyin: String get() = "${stem.pinyin} ${branch.pinyin}"
    val animal: String get() = branch.animal
    val khmerAnimal: String get() = branch.khmerAnimal

    override fun toString(): String = nameZh
}

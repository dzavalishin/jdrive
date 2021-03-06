#if 0
/* $Id: build_industry.h 3135 2005-11-04 16:12:48Z tron $ */

#define MK(x,y, m) {{x, y}, m}

#define MKEND {{-0x80, 0}, 0}

static const IndustryTileTable _tile_table_coal_mine_0[] = {
	MK(1,1, 0),
	MK(1,2, 2),
	MK(0,0, 5),
	MK(1,0, 6),
	MK(2,0, 3),
	MK(2,2, 3),
	MKEND
};

static const IndustryTileTable _tile_table_coal_mine_1[] = {
	MK(1,1, 0),
	MK(1,2, 2),
	MK(2,0, 0),
	MK(2,1, 2),
	MK(1,0, 3),
	MK(0,0, 3),
	MK(0,1, 4),
	MK(0,2, 4),
	MK(2,2, 4),
	MKEND
};

static const IndustryTileTable _tile_table_coal_mine_2[] = {
	MK(0,0, 0),
	MK(0,1, 2),
	MK(0,2, 5),
	MK(1,0, 3),
	MK(1,1, 3),
	MK(1,2, 6),
	MKEND
};

static const IndustryTileTable _tile_table_coal_mine_3[] = {
	MK(0,1, 0),
	MK(0,2, 2),
	MK(0,3, 4),
	MK(1,0, 5),
	MK(1,1, 0),
	MK(1,2, 2),
	MK(1,3, 3),
	MK(2,0, 6),
	MK(2,1, 4),
	MK(2,2, 3),
	MKEND
};

static const IndustryTileTable * const _tile_table_coal_mine[] = {
	_tile_table_coal_mine_0,
	_tile_table_coal_mine_1,
	_tile_table_coal_mine_2,
	_tile_table_coal_mine_3,
};

static const IndustryTileTable _tile_table_power_station_0[] = {
	MK(0,0, 7),
	MK(0,1, 9),
	MK(1,0, 7),
	MK(1,1, 8),
	MK(2,0, 7),
	MK(2,1, 8),
	MK(3,0, 10),
	MK(3,1, 10),
	MKEND
};

static const IndustryTileTable _tile_table_power_station_1[] = {
	MK(0,1, 7),
	MK(0,2, 7),
	MK(1,0, 8),
	MK(1,1, 8),
	MK(1,2, 7),
	MK(2,0, 9),
	MK(2,1, 10),
	MK(2,2, 9),
	MKEND
};

static const IndustryTileTable _tile_table_power_station_2[] = {
	MK(0,0, 7),
	MK(0,1, 7),
	MK(1,0, 9),
	MK(1,1, 8),
	MK(2,0, 10),
	MK(2,1, 9),
	MKEND
};

static const IndustryTileTable * const _tile_table_power_station[] = {
	_tile_table_power_station_0,
	_tile_table_power_station_1,
	_tile_table_power_station_2,
};

static const IndustryTileTable _tile_table_sawmill_0[] = {
	MK(1,0, 14),
	MK(1,1, 12),
	MK(1,2, 11),
	MK(2,0, 14),
	MK(2,1, 13),
	MK(0,0, 15),
	MK(0,1, 15),
	MK(0,2, 12),
	MKEND
};

static const IndustryTileTable _tile_table_sawmill_1[] = {
	MK(0,0, 15),
	MK(0,1, 11),
	MK(0,2, 14),
	MK(1,0, 15),
	MK(1,1, 13),
	MK(1,2, 12),
	MK(2,0, 11),
	MK(2,1, 13),
	MKEND
};

static const IndustryTileTable * const _tile_table_sawmill[] = {
	_tile_table_sawmill_0,
	_tile_table_sawmill_1,
};

static const IndustryTileTable _tile_table_forest_0[] = {
	MK(0,0, 16),
	MK(0,1, 16),
	MK(0,2, 16),
	MK(0,3, 16),
	MK(1,0, 16),
	MK(1,1, 16),
	MK(1,2, 16),
	MK(1,3, 16),
	MK(2,0, 16),
	MK(2,1, 16),
	MK(2,2, 16),
	MK(2,3, 16),
	MK(3,0, 16),
	MK(3,1, 16),
	MK(3,2, 16),
	MK(3,3, 16),
	MK(1,4, 16),
	MK(2,4, 16),
	MKEND
};

static const IndustryTileTable _tile_table_forest_1[] = {
	MK(0,0, 16),
	MK(1,0, 16),
	MK(2,0, 16),
	MK(3,0, 16),
	MK(4,0, 16),
	MK(0,1, 16),
	MK(1,1, 16),
	MK(2,1, 16),
	MK(3,1, 16),
	MK(4,1, 16),
	MK(0,2, 16),
	MK(1,2, 16),
	MK(2,2, 16),
	MK(3,2, 16),
	MK(4,2, 16),
	MK(0,3, 16),
	MK(1,3, 16),
	MK(2,3, 16),
	MK(3,3, 16),
	MK(4,3, 16),
	MK(1,4, 16),
	MK(2,4, 16),
	MK(3,4, 16),
	MKEND
};

static const IndustryTileTable * const _tile_table_forest[] = {
	_tile_table_forest_0,
	_tile_table_forest_1,
};

static const IndustryTileTable _tile_table_oil_refinery_0[] = {
	MK(0,0, 20),
	MK(0,1, 21),
	MK(0,2, 22),
	MK(0,3, 21),
	MK(1,0, 20),
	MK(1,1, 19),
	MK(1,2, 22),
	MK(1,3, 20),
	MK(2,1, 18),
	MK(2,2, 18),
	MK(2,3, 18),
	MK(3,2, 18),
	MK(3,3, 18),
	MK(2,0, 23),
	MK(3,1, 23),
	MKEND
};

static const IndustryTileTable _tile_table_oil_refinery_1[] = {
	MK(0,0, 18),
	MK(0,1, 18),
	MK(0,2, 21),
	MK(0,3, 22),
	MK(0,4, 20),
	MK(1,0, 18),
	MK(1,1, 18),
	MK(1,2, 19),
	MK(1,3, 20),
	MK(2,0, 18),
	MK(2,1, 18),
	MK(2,2, 19),
	MK(2,3, 22),
	MK(1,4, 23),
	MK(2,4, 23),
	MKEND
};

static const IndustryTileTable * const _tile_table_oil_refinery[] = {
	_tile_table_oil_refinery_0,
	_tile_table_oil_refinery_1,
};

static const IndustryTileTable _tile_table_oil_rig_0[] = {
	MK(0,0, 24),
	MK(0,1, 24),
	MK(0,2, 25),
	MK(1,0, 26),
	MK(1,1, 27),
	MK(1,2, 28),
	MK(-4,-5, 255),
	MK(-4,-4, 255),
	MK(-4,-3, 255),
	MK(-4,-2, 255),
	MK(-4,-1, 255),
	MK(-4,0, 255),
	MK(-4,1, 255),
	MK(-4,2, 255),
	MK(-4,3, 255),
	MK(-4,4, 255),
	MK(-4,5, 255),
	MK(-3,5, 255),
	MK(-2,5, 255),
	MK(-1,5, 255),
	MK(0,6, 255),
	MK(1,6, 255),
	MK(2,6, 255),
	MK(3,6, 255),
	MK(4,6, 255),
	MK(5,6, 255),
	MK(5,5, 255),
	MK(5,4, 255),
	MK(5,3, 255),
	MK(5,2, 255),
	MK(5,1, 255),
	MK(5,0, 255),
	MK(5,-1, 255),
	MK(5,-2, 255),
	MK(5,-3, 255),
	MK(5,-4, 255),
	MK(4,-4, 255),
	MK(3,-4, 255),
	MK(2,-4, 255),
	MK(1,-4, 255),
	MK(0,-4, 255),
	MK(-1,-5, 255),
	MK(-2,-5, 255),
	MK(-3,-5, 255),
	MK(2,0, 255),
	MKEND
};

static const IndustryTileTable * const _tile_table_oil_rig[] = {
	_tile_table_oil_rig_0,
};

static const IndustryTileTable _tile_table_factory_0[] = {
	MK(0,0, 39),
	MK(0,1, 40),
	MK(1,0, 41),
	MK(1,1, 42),
	MK(0,2, 39),
	MK(0,3, 40),
	MK(1,2, 41),
	MK(1,3, 42),
	MK(2,1, 39),
	MK(2,2, 40),
	MK(3,1, 41),
	MK(3,2, 42),
	MKEND
};

static const IndustryTileTable _tile_table_factory_1[] = {
	MK(0,0, 39),
	MK(0,1, 40),
	MK(1,0, 41),
	MK(1,1, 42),
	MK(2,0, 39),
	MK(2,1, 40),
	MK(3,0, 41),
	MK(3,1, 42),
	MK(1,2, 39),
	MK(1,3, 40),
	MK(2,2, 41),
	MK(2,3, 42),
	MKEND
};

static const IndustryTileTable * const _tile_table_factory[] = {
	_tile_table_factory_0,
	_tile_table_factory_1,
};

static const IndustryTileTable _tile_table_printing_works_0[] = {
	MK(0,0, 43),
	MK(0,1, 44),
	MK(1,0, 45),
	MK(1,1, 46),
	MK(0,2, 43),
	MK(0,3, 44),
	MK(1,2, 45),
	MK(1,3, 46),
	MK(2,1, 43),
	MK(2,2, 44),
	MK(3,1, 45),
	MK(3,2, 46),
	MKEND
};

static const IndustryTileTable _tile_table_printing_works_1[] = {
	MK(0,0, 43),
	MK(0,1, 44),
	MK(1,0, 45),
	MK(1,1, 46),
	MK(2,0, 43),
	MK(2,1, 44),
	MK(3,0, 45),
	MK(3,1, 46),
	MK(1,2, 43),
	MK(1,3, 44),
	MK(2,2, 45),
	MK(2,3, 46),
	MKEND
};

static const IndustryTileTable * const _tile_table_printing_works[] = {
	_tile_table_printing_works_0,
	_tile_table_printing_works_1,
};

static const IndustryTileTable _tile_table_steel_mill_0[] = {
	MK(2,1, 52),
	MK(2,2, 53),
	MK(3,1, 54),
	MK(3,2, 55),
	MK(0,0, 56),
	MK(1,0, 57),
	MK(0,1, 56),
	MK(1,1, 57),
	MK(0,2, 56),
	MK(1,2, 57),
	MK(2,0, 56),
	MK(3,0, 57),
	MKEND
};

static const IndustryTileTable _tile_table_steel_mill_1[] = {
	MK(0,0, 52),
	MK(0,1, 53),
	MK(1,0, 54),
	MK(1,1, 55),
	MK(2,0, 52),
	MK(2,1, 53),
	MK(3,0, 54),
	MK(3,1, 55),
	MK(0,2, 56),
	MK(1,2, 57),
	MK(2,2, 56),
	MK(3,2, 57),
	MK(1,3, 56),
	MK(2,3, 57),
	MKEND
};

static const IndustryTileTable * const _tile_table_steel_mill[] = {
	_tile_table_steel_mill_0,
	_tile_table_steel_mill_1,
};

static const IndustryTileTable _tile_table_farm_0[] = {
	MK(1,0, 33),
	MK(1,1, 34),
	MK(1,2, 36),
	MK(0,0, 37),
	MK(0,1, 37),
	MK(0,2, 36),
	MK(2,0, 35),
	MK(2,1, 38),
	MK(2,2, 38),
	MKEND
};

static const IndustryTileTable _tile_table_farm_1[] = {
	MK(1,1, 33),
	MK(1,2, 34),
	MK(0,0, 35),
	MK(0,1, 36),
	MK(0,2, 36),
	MK(0,3, 35),
	MK(1,0, 37),
	MK(1,3, 38),
	MK(2,0, 37),
	MK(2,1, 37),
	MK(2,2, 38),
	MK(2,3, 38),
	MKEND
};

static const IndustryTileTable _tile_table_farm_2[] = {
	MK(2,0, 33),
	MK(2,1, 34),
	MK(0,0, 36),
	MK(0,1, 36),
	MK(0,2, 37),
	MK(0,3, 37),
	MK(1,0, 35),
	MK(1,1, 38),
	MK(1,2, 38),
	MK(1,3, 37),
	MK(2,2, 37),
	MK(2,3, 35),
	MKEND
};

static const IndustryTileTable * const _tile_table_farm[] = {
	_tile_table_farm_0,
	_tile_table_farm_1,
	_tile_table_farm_2,
};

static const IndustryTileTable _tile_table_copper_mine_0[] = {
	MK(0,0, 47),
	MK(0,1, 49),
	MK(0,2, 51),
	MK(1,0, 47),
	MK(1,1, 49),
	MK(1,2, 50),
	MK(2,0, 51),
	MK(2,1, 51),
	MKEND
};

static const IndustryTileTable _tile_table_copper_mine_1[] = {
	MK(0,0, 50),
	MK(0,1, 47),
	MK(0,2, 49),
	MK(1,0, 47),
	MK(1,1, 49),
	MK(1,2, 51),
	MK(2,0, 51),
	MK(2,1, 47),
	MK(2,2, 49),
	MKEND
};

static const IndustryTileTable * const _tile_table_copper_mine[] = {
	_tile_table_copper_mine_0,
	_tile_table_copper_mine_1,
};

static const IndustryTileTable _tile_table_oil_well_0[] = {
	MK(0,0, 29),
	MK(1,0, 29),
	MK(2,0, 29),
	MK(0,1, 29),
	MK(0,2, 29),
	MKEND
};

static const IndustryTileTable _tile_table_oil_well_1[] = {
	MK(0,0, 29),
	MK(1,0, 29),
	MK(1,1, 29),
	MK(2,2, 29),
	MK(2,3, 29),
	MKEND
};

static const IndustryTileTable * const _tile_table_oil_well[] = {
	_tile_table_oil_well_0,
	_tile_table_oil_well_1,
};

static const IndustryTileTable _tile_table_bank_0[] = {
	MK(0,0, 58),
	MK(1,0, 59),
	MKEND
};

static const IndustryTileTable * const _tile_table_bank[] = {
	_tile_table_bank_0,
};

static const IndustryTileTable _tile_table_food_process_0[] = {
	MK(0,0, 60),
	MK(1,0, 60),
	MK(2,0, 60),
	MK(0,1, 60),
	MK(1,1, 60),
	MK(2,1, 60),
	MK(0,2, 61),
	MK(1,2, 61),
	MK(2,2, 63),
	MK(0,3, 62),
	MK(1,3, 62),
	MK(2,3, 63),
	MKEND
};

static const IndustryTileTable _tile_table_food_process_1[] = {
	MK(0,0, 61),
	MK(1,0, 60),
	MK(2,0, 61),
	MK(3,0, 61),
	MK(0,1, 62),
	MK(1,1, 63),
	MK(2,1, 63),
	MK(3,1, 63),
	MK(0,2, 60),
	MK(1,2, 60),
	MK(2,2, 60),
	MK(3,2, 60),
	MK(0,3, 62),
	MK(1,3, 62),
	MKEND
};

static const IndustryTileTable * const _tile_table_food_process[] = {
	_tile_table_food_process_0,
	_tile_table_food_process_1,
};

static const IndustryTileTable _tile_table_paper_mill_0[] = {
	MK(0,0, 64),
	MK(1,0, 65),
	MK(2,0, 66),
	MK(3,0, 67),
	MK(0,1, 68),
	MK(1,1, 69),
	MK(2,1, 67),
	MK(3,1, 67),
	MK(0,2, 66),
	MK(1,2, 71),
	MK(2,2, 71),
	MK(3,2, 70),
	MKEND
};

static const IndustryTileTable * const _tile_table_paper_mill[] = {
	_tile_table_paper_mill_0,
};

static const IndustryTileTable _tile_table_gold_mine_0[] = {
	MK(0,0, 72),
	MK(0,1, 73),
	MK(0,2, 74),
	MK(0,3, 75),
	MK(1,0, 76),
	MK(1,1, 77),
	MK(1,2, 78),
	MK(1,3, 79),
	MK(2,0, 80),
	MK(2,1, 81),
	MK(2,2, 82),
	MK(2,3, 83),
	MK(3,0, 84),
	MK(3,1, 85),
	MK(3,2, 86),
	MK(3,3, 87),
	MKEND
};

static const IndustryTileTable * const _tile_table_gold_mine[] = {
	_tile_table_gold_mine_0,
};

static const IndustryTileTable _tile_table_bank2_0[] = {
	MK(0,0, 89),
	MK(1,0, 90),
	MKEND
};

static const IndustryTileTable * const _tile_table_bank2[] = {
	_tile_table_bank2_0,
};

static const IndustryTileTable _tile_table_diamond_mine_0[] = {
	MK(0,0, 91),
	MK(0,1, 92),
	MK(0,2, 93),
	MK(1,0, 94),
	MK(1,1, 95),
	MK(1,2, 96),
	MK(2,0, 97),
	MK(2,1, 98),
	MK(2,2, 99),
	MKEND
};

static const IndustryTileTable * const _tile_table_diamond_mine[] = {
	_tile_table_diamond_mine_0,
};

static const IndustryTileTable _tile_table_iron_mine_0[] = {
	MK(0,0, 100),
	MK(0,1, 101),
	MK(0,2, 102),
	MK(0,3, 103),
	MK(1,0, 104),
	MK(1,1, 105),
	MK(1,2, 106),
	MK(1,3, 107),
	MK(2,0, 108),
	MK(2,1, 109),
	MK(2,2, 110),
	MK(2,3, 111),
	MK(3,0, 112),
	MK(3,1, 113),
	MK(3,2, 114),
	MK(3,3, 115),
	MKEND
};

static const IndustryTileTable * const _tile_table_iron_mine[] = {
	_tile_table_iron_mine_0,
};

static const IndustryTileTable _tile_table_fruit_plantation_0[] = {
	MK(0,0, 116),
	MK(0,1, 116),
	MK(0,2, 116),
	MK(0,3, 116),
	MK(1,0, 116),
	MK(1,1, 116),
	MK(1,2, 116),
	MK(1,3, 116),
	MK(2,0, 116),
	MK(2,1, 116),
	MK(2,2, 116),
	MK(2,3, 116),
	MK(3,0, 116),
	MK(3,1, 116),
	MK(3,2, 116),
	MK(3,3, 116),
	MK(4,0, 116),
	MK(4,1, 116),
	MK(4,2, 116),
	MK(4,3, 116),
	MKEND
};

static const IndustryTileTable * const _tile_table_fruit_plantation[] = {
	_tile_table_fruit_plantation_0,
};

static const IndustryTileTable _tile_table_rubber_plantation_0[] = {
	MK(0,0, 117),
	MK(0,1, 117),
	MK(0,2, 117),
	MK(0,3, 117),
	MK(1,0, 117),
	MK(1,1, 117),
	MK(1,2, 117),
	MK(1,3, 117),
	MK(2,0, 117),
	MK(2,1, 117),
	MK(2,2, 117),
	MK(2,3, 117),
	MK(3,0, 117),
	MK(3,1, 117),
	MK(3,2, 117),
	MK(3,3, 117),
	MK(4,0, 117),
	MK(4,1, 117),
	MK(4,2, 117),
	MK(4,3, 117),
	MKEND
};

static const IndustryTileTable * const _tile_table_rubber_plantation[] = {
	_tile_table_rubber_plantation_0,
};

static const IndustryTileTable _tile_table_water_supply_0[] = {
	MK(0,0, 118),
	MK(0,1, 119),
	MK(1,0, 118),
	MK(1,1, 119),
	MKEND
};

static const IndustryTileTable * const _tile_table_water_supply[] = {
	_tile_table_water_supply_0,
};

static const IndustryTileTable _tile_table_water_tower_0[] = {
	MK(0,0, 120),
	MKEND
};

static const IndustryTileTable * const _tile_table_water_tower[] = {
	_tile_table_water_tower_0,
};

static const IndustryTileTable _tile_table_factory2_0[] = {
	MK(0,0, 121),
	MK(0,1, 122),
	MK(1,0, 123),
	MK(1,1, 124),
	MK(0,2, 121),
	MK(0,3, 122),
	MK(1,2, 123),
	MK(1,3, 124),
	MKEND
};

static const IndustryTileTable _tile_table_factory2_1[] = {
	MK(0,0, 121),
	MK(0,1, 122),
	MK(1,0, 123),
	MK(1,1, 124),
	MK(2,0, 121),
	MK(2,1, 122),
	MK(3,0, 123),
	MK(3,1, 124),
	MKEND
};

static const IndustryTileTable * const _tile_table_factory2[] = {
	_tile_table_factory2_0,
	_tile_table_factory2_1,
};

static const IndustryTileTable _tile_table_farm2_0[] = {
	MK(1,0, 33),
	MK(1,1, 34),
	MK(1,2, 36),
	MK(0,0, 37),
	MK(0,1, 37),
	MK(0,2, 36),
	MK(2,0, 35),
	MK(2,1, 38),
	MK(2,2, 38),
	MKEND
};

static const IndustryTileTable _tile_table_farm2_1[] = {
	MK(1,1, 33),
	MK(1,2, 34),
	MK(0,0, 35),
	MK(0,1, 36),
	MK(0,2, 36),
	MK(0,3, 35),
	MK(1,0, 37),
	MK(1,3, 38),
	MK(2,0, 37),
	MK(2,1, 37),
	MK(2,2, 38),
	MK(2,3, 38),
	MKEND
};

static const IndustryTileTable _tile_table_farm2_2[] = {
	MK(2,0, 33),
	MK(2,1, 34),
	MK(0,0, 36),
	MK(0,1, 36),
	MK(0,2, 37),
	MK(0,3, 37),
	MK(1,0, 35),
	MK(1,1, 38),
	MK(1,2, 38),
	MK(1,3, 37),
	MK(2,2, 37),
	MK(2,3, 35),
	MKEND
};

static const IndustryTileTable * const _tile_table_farm2[] = {
	_tile_table_farm2_0,
	_tile_table_farm2_1,
	_tile_table_farm2_2,
};

static const IndustryTileTable _tile_table_lumber_mill_0[] = {
	MK(0,0, 125),
	MK(0,1, 126),
	MK(1,0, 127),
	MK(1,1, 128),
	MKEND
};

static const IndustryTileTable * const _tile_table_lumber_mill[] = {
	_tile_table_lumber_mill_0,
};

static const IndustryTileTable _tile_table_cotton_candy_0[] = {
	MK(0,0, 129),
	MK(0,1, 129),
	MK(0,2, 129),
	MK(0,3, 129),
	MK(1,0, 129),
	MK(1,1, 129),
	MK(1,2, 129),
	MK(1,3, 129),
	MK(2,0, 129),
	MK(2,1, 129),
	MK(2,2, 129),
	MK(2,3, 129),
	MK(3,0, 129),
	MK(3,1, 129),
	MK(3,2, 129),
	MK(3,3, 129),
	MK(1,4, 129),
	MK(2,4, 129),
	MKEND
};

static const IndustryTileTable _tile_table_cotton_candy_1[] = {
	MK(0,0, 129),
	MK(1,0, 129),
	MK(2,0, 129),
	MK(3,0, 129),
	MK(4,0, 129),
	MK(0,1, 129),
	MK(1,1, 129),
	MK(2,1, 129),
	MK(3,1, 129),
	MK(4,1, 129),
	MK(0,2, 129),
	MK(1,2, 129),
	MK(2,2, 129),
	MK(3,2, 129),
	MK(4,2, 129),
	MK(0,3, 129),
	MK(1,3, 129),
	MK(2,3, 129),
	MK(3,3, 129),
	MK(4,3, 129),
	MK(1,4, 129),
	MK(2,4, 129),
	MK(3,4, 129),
	MKEND
};

static const IndustryTileTable * const _tile_table_cotton_candy[] = {
	_tile_table_cotton_candy_0,
	_tile_table_cotton_candy_1,
};

static const IndustryTileTable _tile_table_candy_factory_0[] = {
	MK(0,0, 131),
	MK(0,1, 132),
	MK(1,0, 133),
	MK(1,1, 134),
	MK(0,2, 131),
	MK(0,3, 132),
	MK(1,2, 133),
	MK(1,3, 134),
	MK(2,1, 131),
	MK(2,2, 132),
	MK(3,1, 133),
	MK(3,2, 134),
	MKEND
};

static const IndustryTileTable _tile_table_candy_factory_1[] = {
	MK(0,0, 131),
	MK(0,1, 132),
	MK(1,0, 133),
	MK(1,1, 134),
	MK(2,0, 131),
	MK(2,1, 132),
	MK(3,0, 133),
	MK(3,1, 134),
	MK(1,2, 131),
	MK(1,3, 132),
	MK(2,2, 133),
	MK(2,3, 134),
	MKEND
};

static const IndustryTileTable * const _tile_table_candy_factory[] = {
	_tile_table_candy_factory_0,
	_tile_table_candy_factory_1,
};

static const IndustryTileTable _tile_table_battery_farm_0[] = {
	MK(0,0, 135),
	MK(0,1, 135),
	MK(0,2, 135),
	MK(0,3, 135),
	MK(1,0, 135),
	MK(1,1, 135),
	MK(1,2, 135),
	MK(1,3, 135),
	MK(2,0, 135),
	MK(2,1, 135),
	MK(2,2, 135),
	MK(2,3, 135),
	MK(3,0, 135),
	MK(3,1, 135),
	MK(3,2, 135),
	MK(3,3, 135),
	MK(4,0, 135),
	MK(4,1, 135),
	MK(4,2, 135),
	MK(4,3, 135),
	MKEND
};

static const IndustryTileTable * const _tile_table_battery_farm[] = {
	_tile_table_battery_farm_0,
};

static const IndustryTileTable _tile_table_cola_wells_0[] = {
	MK(0,0, 137),
	MK(0,1, 137),
	MK(0,2, 137),
	MK(1,0, 137),
	MK(1,1, 137),
	MK(1,2, 137),
	MK(2,1, 137),
	MK(2,2, 137),
	MKEND
};

static const IndustryTileTable _tile_table_cola_wells_1[] = {
	MK(0,1, 137),
	MK(0,2, 137),
	MK(0,3, 137),
	MK(1,0, 137),
	MK(1,1, 137),
	MK(1,2, 137),
	MK(2,1, 137),
	MKEND
};

static const IndustryTileTable * const _tile_table_cola_wells[] = {
	_tile_table_cola_wells_0,
	_tile_table_cola_wells_1,
};

static const IndustryTileTable _tile_table_toy_shop_0[] = {
	MK(0,0, 138),
	MK(0,1, 139),
	MK(1,0, 140),
	MK(1,1, 141),
	MKEND
};

static const IndustryTileTable * const _tile_table_toy_shop[] = {
	_tile_table_toy_shop_0,
};

static const IndustryTileTable _tile_table_toy_factory_0[] = {
	MK(0,0, 147),
	MK(0,1, 142),
	MK(1,0, 147),
	MK(1,1, 143),
	MK(2,0, 147),
	MK(2,1, 144),
	MK(3,0, 146),
	MK(3,1, 145),
	MKEND
};

static const IndustryTileTable * const _tile_table_toy_factory[] = {
	_tile_table_toy_factory_0,
};

static const IndustryTileTable _tile_table_plastic_fountain_0[] = {
	MK(0,0, 148),
	MK(0,1, 151),
	MK(0,2, 154),
	MKEND
};

static const IndustryTileTable _tile_table_plastic_fountain_1[] = {
	MK(0,0, 148),
	MK(1,0, 151),
	MK(2,0, 154),
	MKEND
};

static const IndustryTileTable * const _tile_table_plastic_fountain[] = {
	_tile_table_plastic_fountain_0,
	_tile_table_plastic_fountain_1,
};

static const IndustryTileTable _tile_table_fizzy_drink_0[] = {
	MK(0,0, 156),
	MK(0,1, 157),
	MK(1,0, 158),
	MK(1,1, 159),
	MKEND
};

static const IndustryTileTable * const _tile_table_fizzy_drink[] = {
	_tile_table_fizzy_drink_0,
};

static const IndustryTileTable _tile_table_bubble_generator_0[] = {
	MK(0,0, 163),
	MK(0,1, 160),
	MK(1,0, 163),
	MK(1,1, 161),
	MK(2,0, 163),
	MK(2,1, 162),
	MK(0,2, 163),
	MK(0,3, 160),
	MK(1,2, 163),
	MK(1,3, 161),
	MK(2,2, 163),
	MK(2,3, 162),
	MKEND
};

static const IndustryTileTable * const _tile_table_bubble_generator[] = {
	_tile_table_bubble_generator_0,
};

static const IndustryTileTable _tile_table_toffee_quarry_0[] = {
	MK(0,0, 164),
	MK(1,0, 165),
	MK(2,0, 166),
	MKEND
};

static const IndustryTileTable * const _tile_table_toffee_quarry[] = {
	_tile_table_toffee_quarry_0,
};

static const IndustryTileTable _tile_table_sugar_mine_0[] = {
	MK(0,0, 167),
	MK(0,1, 168),
	MK(1,0, 169),
	MK(1,1, 170),
	MK(2,0, 171),
	MK(2,1, 172),
	MK(3,0, 173),
	MK(3,1, 174),
	MKEND
};

static const IndustryTileTable * const _tile_table_sugar_mine[] = {
	_tile_table_sugar_mine_0,
};

#undef MK
#undef MKEND

#define MK(tbl, a,b,c, p1,p2, r1,r2, a1,a2,a3, proc) {tbl,lengthof(tbl),a,b,c,{p1,p2},{r1,r2},{a1,a2,a3},proc}

static const IndustrySpec _industry_spec[37] = {
/*        name                    not close to   produce prodrate  accepts     checkproc */
	MK(_tile_table_coal_mine,         1,255,255,    1,255,  15, 0,  255,255,255,  0),
	MK(_tile_table_power_station,     0,255,255,  255,255,   0, 0,    1,255,255,  0),
	MK(_tile_table_sawmill,           3,255,255,    5,255,   0, 0,    7,255,255,  0),
	MK(_tile_table_forest,            2, 14,255,    7,255,  13, 0,  255,255,255,  1),
	MK(_tile_table_oil_refinery,      5,255,255,    5,255,   0, 0,    3,255,255,  2),
	MK(_tile_table_oil_rig,           4,255,255,    3,  0,  15, 2,  255,255,255,  2),
	MK(_tile_table_factory,           9,  8,255,    5,255,   0, 0,    4,  6,  9,  0),
	MK(_tile_table_printing_works,   14,255,255,    5,255,   0, 0,    9,255,255,  0),
	MK(_tile_table_steel_mill,       18,  6,255,    9,255,   0, 0,    8,255,255,  0),
	MK(_tile_table_farm,              6, 13,255,    6,  4,  10,10,  255,255,255,  3),
	MK(_tile_table_copper_mine,      23,255,255,    8,255,  10, 0,  255,255,255,  0),
	MK(_tile_table_oil_well,          4,255,255,    3,255,  12, 0,  255,255,255,  0),
	MK(_tile_table_bank,             12,255,255,   10,255,   6, 0,   10,255,255,  0),
	MK(_tile_table_food_process,      9, 19, 24,   11,255,   0, 0,    4,  6,255,  0),
	MK(_tile_table_paper_mill,        3,  7,255,    9,255,   0, 0,    7,255,255,  0),
	MK(_tile_table_gold_mine,        16,255,255,   10,255,   7, 0,  255,255,255,  0),
	MK(_tile_table_bank2,            15, 17,255,  255,255,   0, 0,   10,255,255,  0),
	MK(_tile_table_diamond_mine,     16,255,255,   10,255,   7, 0,  255,255,255,  0),
	MK(_tile_table_iron_mine,         8,255,255,    8,255,  10, 0,  255,255,255,  0),
	MK(_tile_table_fruit_plantation, 13,255,255,    4,255,  10, 0,  255,255,255,  4),
	MK(_tile_table_rubber_plantation,23,255,255,    1,255,  10, 0,  255,255,255,  4),
	MK(_tile_table_water_supply,     22,255,255,    9,255,  12, 0,  255,255,255,  5),
	MK(_tile_table_water_tower,      21,255,255,  255,255,   0, 0,    9,255,255,  5),
	MK(_tile_table_factory2,         10, 20, 25,    5,255,   0, 0,    1,  8,  7,  4),
	MK(_tile_table_farm2,            13,255,255,    6,255,  11, 0,  255,255,255,  4),
	MK(_tile_table_lumber_mill,      23,255,255,    7,255,   0, 0,  255,255,255,  6),
	MK(_tile_table_cotton_candy,     27,255,255,    8,255,  13, 0,  255,255,255,  0),
	MK(_tile_table_candy_factory,    26, 35, 36,    5,255,   0, 0,    1,  6,  8,  0),
	MK(_tile_table_battery_farm,     31,255,255,    4,255,  11, 0,  255,255,255,  0),
	MK(_tile_table_cola_wells,       33,255,255,    7,255,  12, 0,  255,255,255,  0),
	MK(_tile_table_toy_shop,         31,255,255,  255,255,   0, 0,    3,255,255,  0),
	MK(_tile_table_toy_factory,      30, 28, 32,    3,255,   0, 0,   10,  4,255,  0),
	MK(_tile_table_plastic_fountain, 31,255,255,   10,255,  14, 0,  255,255,255,  0),
	MK(_tile_table_fizzy_drink,      29, 34,255,   11,255,   0, 0,    7,  9,255,  0),
	MK(_tile_table_bubble_generator, 33,255,255,    9,255,  13, 0,  255,255,255,  7),
	MK(_tile_table_toffee_quarry,    27,255,255,    6,255,  10, 0,  255,255,255,  0),
	MK(_tile_table_sugar_mine,       27,255,255,    1,255,  11, 0,  255,255,255,  0),
};
#undef MK

const	byte _industry_type_costs[37] = {
	210, 30,   28, 200,  31, 240,  26,  26,  27, 250, 205, 220, 193,  26,
	28,  208,  19, 213, 220, 225, 218, 199,  14,  26, 250, 17,  195,  26,
	187, 193,  17,  20, 192,  22, 203, 213, 210
};

const byte _build_industry_types[4][12] = {
	{ 1,  2, 4,  6, 8, 0, 3, 5,  9, 11, 18 },
	{ 1, 14, 4, 13, 7, 0, 3, 9, 11, 15 },
	{ 25, 13, 4, 23, 22, 11, 17, 10, 24, 19, 20, 21 },
	{ 27, 30, 31, 33, 26, 28, 29, 32, 34, 35, 36 },
};


static const byte _industry_create_table_0[] = {
	4, 4,
	5, 3,
	5, 2,
	8, 0,
	5, 1,
	4, 11,
	5, 18,
	9, 9,
	5, 8,
	5, 6,
	0
};

static const byte _industry_create_table_1[] = {
	4, 4,
	5, 3,
	5, 14,
	8, 0,
	5, 1,
	5, 11,
	5, 13,
	9, 9,
	4, 15,
	5, 7,
	6, 16,
	0
};

static const byte _industry_create_table_2[] = {
	4, 4,
	5, 11,
	5, 16,
	4, 13,
	4, 17,
	4, 10,
	4, 19,
	4, 20,
	4, 21,
	8, 22,
	4, 13,
	4, 23,
	2, 24,
	0
};

static const byte _industry_create_table_3[] = {
	5, 26,
	5, 27,
	4, 28,
	5, 29,
	4, 30,
	5, 31,
	5, 32,
	4, 33,
	5, 34,
	5, 35,
	4, 36,
	0
};

static const byte * const _industry_create_table[4] = {
	_industry_create_table_0,
	_industry_create_table_1,
	_industry_create_table_2,
	_industry_create_table_3,
};


#define PAS CT_PASSENGERS

#define COL CT_COAL
#define RUB CT_RUBBER
#define SUG CT_SUGAR

#define MAL CT_MAIL

#define OIL CT_OIL
#define TOY CT_TOYS

#define LIV CT_LIVESTOCK // Fruit too
#define BAT CT_BATTERIES

#define GRA CT_GRAIN
#define WHT CT_WHEAT // Maize too
#define TOF CT_TOFFEE

#define WOD CT_WOOD
#define CLA CT_COLA

#define IRN CT_IRON_ORE
#define COP CT_COPPER_ORE
#define CCY CT_COTTON_CANDY

#define STL CT_STEEL
#define PAP CT_PAPER
#define WAT CT_WATER
#define BBL CT_BUBBLES

#define VAL CT_VALUABLES
#define GLD CT_GOLD // Diamonds too
#define PLC CT_PLASTIC

#define INV CT_INVALID

static const CargoID _industry_map5_accepts_1[] = {
	INV, INV, INV, PAS, INV, INV, INV, INV,
	PAS, INV, INV, PAS, PAS, PAS, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, PAS,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, PAS, PAS, INV, INV, INV, INV, GRA,
	GRA, GRA, GRA, INV, INV, INV, INV, INV,
	INV, INV, PAS, PAS, PAS, PAS, PAS, PAS,
	PAS, PAS, PAS, PAS, WHT, WHT, WHT, WHT,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, COP, COP, COP, COP, INV, INV, INV,
	INV, INV, INV, CCY, CCY, CCY, CCY, INV,
	INV, INV, INV, INV, INV, INV, BAT, BAT,
	BAT, BAT, BAT, BAT, INV, INV, INV, INV,
	INV, INV, INV, INV, BBL, BBL, BBL, BBL,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV
};


static const CargoID _industry_map5_accepts_2[] = {
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, STL,
	STL, STL, STL, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, WOD, WOD, WOD, WOD, INV, INV, INV,
	INV, INV, INV, TOF, TOF, TOF, TOF, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV
};

static const CargoID _industry_map5_accepts_3[] = {
	INV, INV, INV, INV, INV, INV, INV, INV,
	COL, INV, INV, INV, INV, WOD, INV, INV,
	INV, INV, INV, INV, OIL, INV, INV, INV,
	PAS, MAL, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, LIV,
	LIV, LIV, LIV, PAP, PAP, PAP, PAP, INV,
	INV, INV, INV, INV, IRN, IRN, IRN, IRN,
	IRN, IRN, VAL, VAL, LIV, LIV, LIV, LIV,
	WOD, WOD, WOD, WOD, WOD, WOD, WOD, WOD,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, GLD, GLD, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV, INV,
	WAT, RUB, RUB, RUB, RUB, INV, INV, INV,
	INV, INV, INV, SUG, SUG, SUG, SUG, INV,
	INV, INV, TOY, TOY, TOY, TOY, PLC, PLC,
	PLC, PLC, PLC, PLC, INV, INV, INV, INV,
	INV, INV, INV, INV, CLA, CLA, CLA, CLA,
	INV, INV, INV, INV, INV, INV, INV, INV,
	INV, INV, INV, INV, INV, INV, INV
};

#undef PAS
#undef COL
#undef RUB
#undef SUG
#undef MAL
#undef OIL
#undef TOY
#undef LIV
#undef BAT
#undef GRA
#undef WHT
#undef TOF
#undef WOD
#undef CLA
#undef IRN
#undef COP
#undef CCY
#undef STL
#undef PAP
#undef WAT
#undef BBL
#undef VAL
#undef GLD
#undef PLC
#undef INV

function calc_with_values() {
    var hp_in = parseInt(document.getElementById("HP").value) || 0;
    var ac_in = parseInt(document.getElementById("AC").value) || 0;
    var dmg_in = parseInt(document.getElementById("Dmg").value) || 0;
    var atk_in = parseInt(document.getElementById("Atk").value) || 0;

    var defense = to_defense_cr(hp_in, ac_in);
    var offense = to_offense_cr(dmg_in, atk_in);
    var average = cr_average(defense, offense);
    document.getElementById("defense").innerText = "Defense: " + lookup_cr_view(defense);
    document.getElementById("offense").innerText = "Offense: " + lookup_cr_view(offense);
    document.getElementById("average").innerText = "Average: " + lookup_cr_view(average);
}

function lookup_cr_view(cr) {
    switch (cr) {
        case undefined:
        return "0";
        case 0.125:
        return "1/8";
        break;
        case 0.25:
        return "1/4";
        break;
        case 0.5:
        return "1/2";
        break;
        default:
        return String(cr);
        break;
    }
}

function cr_average(d_cr, o_cr) {
    if (d_cr == 0) { return o_cr; }
    else if (o_cr == 0) { return d_cr; }
    var average = (d_cr + o_cr) / 2;
    var next_highest_index = null;
    for (n in crs) {
        if (crs[n] > average) { next_highest_index = n; break; }
    }
    if (next_highest_index) {
        var low = crs[next_highest_index - 1] || 0;
        var high = crs[next_highest_index];
        var low_diff = Math.abs(average - low);
        var high_diff = Math.abs(average - high);

        if (low_diff == 0) { return low; }
        else if (high_diff == 0) { return high; }
        else if (low_diff > high_diff) { return low; }
        else { return high; }
    }
}

function to_defense_cr(hp, ac) {
    var defense_lookup_pair = defense_cr_value_table[hp];
    if (defense_lookup_pair) {
        var cr_lookup = defense_lookup_pair[0];
        var cr_ac = defense_lookup_pair[1];
        var ac_diff = ac - cr_ac;
        var diff = Math.round(Math.floor(Math.abs(ac_diff) / 2))
        if (ac_diff < 0) {
            return crs[Math.max(0, cr_lookup - diff)];
        } else {
            return crs[Math.min(30, cr_lookup + diff)];
        }
    }
}

function to_offense_cr(damage, atk) {
    var offense_lookup_pair = offense_cr_value_table[damage];
    if (offense_lookup_pair) {
        var cr_lookup = offense_lookup_pair[0];
        var cr_atk = offense_lookup_pair[1];
        var atk_diff = atk - cr_atk;
        var diff = Math.round(Math.floor(Math.abs(atk_diff) / 2))
        if (atk_diff < 0) {
            return crs[Math.max(0, cr_lookup - diff)];
        } else {
            return crs[Math.min(30, cr_lookup + diff)];
        }
    }
}

function range(from, to) {
    return Array.apply(null, Array(to)).map(function (_, i) {return from+i;});
}

function to_ranges(ranges) {
    var lookup = {};
    for (var cr = 0; cr < ranges.length; cr++) {
        var lookup_range = range(ranges[cr][0], ranges[cr][1] + 1);
        for (n in lookup_range) {
            lookup[String(lookup_range[n])] = [cr + 1, ranges[cr][2]];
        }
    }
    return lookup;
}

var defense_cr_value_table = to_ranges(
    [[1, 6, 11],
     [7, 35, 13],
     [36, 49, 13],
     [50, 70, 13],
     [71, 85, 13],
     [86, 100, 13],
     [101, 115, 13],
     [116, 130, 14],
     [131, 145, 15],
     [146, 160, 15],
     [161, 175, 15],
     [176, 190, 16],
     [191, 205, 16],
     [206, 220, 17],
     [221, 235, 17],
     [236, 250, 17],
     [251, 265, 18],
     [266, 280, 18],
     [281, 295, 18],
     [296, 310, 18],
     [311, 325, 19],
     [326, 340, 19],
     [341, 355, 19],
     [356, 400, 19],
     [401, 445, 19],
     [446, 490, 19],
     [491, 535, 19],
     [536, 580, 19],
     [581, 625, 19],
     [626, 670, 19],
     [671, 715, 19],
     [716, 760, 19],
     [761, 805, 19],
     [806, 850, 19]]);

var offense_cr_value_table = to_ranges(
    [[0, 1, 1],
     [2, 3, 3],
     [4, 5, 3],
     [6, 8, 3],
     [9, 14, 3],
     [15, 20, 3],
     [21, 26, 4],
     [27, 32, 5],
     [33, 38, 6],
     [39, 44, 6],
     [45, 50, 6],
     [51, 56, 7],
     [57, 62, 7],
     [63, 68, 7],
     [69, 74, 8],
     [75, 80, 8],
     [81, 86, 8],
     [87, 92, 8],
     [93, 98, 8],
     [99, 104, 9],
     [105, 110, 10],
     [111, 116, 10],
     [117, 122, 10],
     [123, 140, 10],
     [141, 158, 11],
     [159, 176, 11],
     [177, 194, 11],
     [195, 212, 12],
     [213, 230, 12],
     [231, 248, 12],
     [249, 266, 13],
     [267, 284, 13],
     [285, 302, 13],
     [303, 320, 14]]);

var crs = {};
var rows = [[1, 0],
           [2, 1 / 8],
           [3, 1 / 4],
           [4, 1 / 2]].concat(
               Array.apply(null, Array(30)).map(
                   function(_, i) { return [i+5, i+1]; }));
for (r in rows) {
    crs[String(rows[r][0])] = rows[r][1];
}

import json

wilks_m_coeff = [-216.0475144, 16.2606339, -0.002388645, -0.00113732, 7.01863e-6, -1.291e-8]
wilks_f_coeff = [594.31747775582, -27.23842536447, 0.82112226871, -0.00930733913, 4.731582e-5,-9.054e-8]

wilks2_m_coeff = [47.46178854, 8.472061379, 0.07369410346, -0.001395833811, 7.07665973070743e-6, -1.20804336482315e-8]
wilks2_f_coeff = [-125.4255398, 13.71219419, -0.03307250631, -0.001050400051, 9.38773881462799e-6, -2.3334613884954e-8]

dots_m_coeff = [-307.75076, 24.0900756, -0.1918759221, 0.0007391293, -0.000001093]
dots_f_coeff = [-57.96288, 13.6175032, -0.1126655495, 0.0005158568, -0.0000010706]

bench_standard_m = [0.50, 0.75, 1.25, 1.75, 2.00]
bench_standard_f = [0.25, 0.50, 0.75, 1.00, 1.50]

squat_standard_m = [0.75, 1.25, 1.50, 2.25, 2.75]
squat_standard_f = [0.50, 0.75, 1.25, 1.50, 2.00]

deadlift_standard_m = [1.00, 1.50, 2.00, 2.50, 3.00]
deadlift_standard_f = [0.50, 1.00, 1.25, 1.75, 2.50]

def validateInputs(exerciseType, sex, body_weight, lift_weight):
    if (sex != "male" and sex != "female") or (body_weight <= 0 or lift_weight <= 0) or (exerciseType != "Bench Press" and exerciseType != "Squat" and exerciseType != "Deadlift"):
        raise ValueError("Invalid Input!")

def lbs_to_kg(lbs):
    return lbs*0.45359291

def calc_wilks_score(sex, body_weight, lift_weight):
    bw_kg = lbs_to_kg(body_weight)
    lw_kg = lbs_to_kg(lift_weight)
    if sex == "male":
        coeffs = wilks_m_coeff
    else:
        coeffs = wilks_f_coeff
    return lw_kg*(500/(coeffs[0] +
                       coeffs[1]*pow(bw_kg, 1) +
                       coeffs[2]*pow(bw_kg, 2) +
                       coeffs[3]*pow(bw_kg, 3) +
                       coeffs[4]*pow(bw_kg, 4) +
                       coeffs[5]*pow(bw_kg, 5)))

def calc_wilks2_score(sex, body_weight, lift_weight):
    bw_kg = lbs_to_kg(body_weight)
    lw_kg = lbs_to_kg(lift_weight)
    if sex == "male":
        coeffs = wilks2_m_coeff
    else:
        coeffs = wilks2_f_coeff
    return lw_kg*(600/(coeffs[0] +
                       coeffs[1]*pow(bw_kg, 1) +
                       coeffs[2]*pow(bw_kg, 2) +
                       coeffs[3]*pow(bw_kg, 3) +
                       coeffs[4]*pow(bw_kg, 4) +
                       coeffs[5]*pow(bw_kg, 5)))

def calc_dots_score(sex, body_weight, lift_weight):
    bw_kg = lbs_to_kg(body_weight)
    lw_kg = lbs_to_kg(lift_weight)
    if sex == "male":
        coeffs = dots_m_coeff
    else:
        coeffs = dots_f_coeff
    return lw_kg*(500/(coeffs[0] +
                       coeffs[1]*pow(bw_kg, 1) +
                       coeffs[2]*pow(bw_kg, 2) +
                       coeffs[3]*pow(bw_kg, 3) +
                       coeffs[4]*pow(bw_kg, 4)))

def calc_body_weight_ratio(body_weight, lift_weight):
    return lift_weight/body_weight;

def determine_level(bw_ratio, boundaries):
    if bw_ratio < boundaries[0]:
        return "Untrained"
    elif bw_ratio < boundaries[1]:
        return "Beginner"
    elif bw_ratio < boundaries[2]:
        return "Novice"
    elif bw_ratio < boundaries[3]:
        return "Intermediate"
    elif bw_ratio < boundaries[4]:
        return "Advanced"
    else:
        return "Elite"

def calc_bench_level(sex, body_weight, lift_weight):
    bw_ratio = calc_body_weight_ratio(body_weight, lift_weight)
    if sex == "male":
        return determine_level(bw_ratio, bench_standard_m)
    else:
        return determine_level(bw_ratio, bench_standard_f)

def calc_squat_level(sex, body_weight, lift_weight):
    bw_ratio = calc_body_weight_ratio(body_weight, lift_weight)
    if sex == "male":
        return determine_level(bw_ratio, squat_standard_m)
    else:
        return determine_level(bw_ratio, squat_standard_f)

def calc_deadlift_level(sex, body_weight, lift_weight):
    bw_ratio = calc_body_weight_ratio(body_weight, lift_weight)
    if sex == "male":
        return determine_level(bw_ratio, deadlift_standard_m)
    else:
        return determine_level(bw_ratio, deadlift_standard_f)

def get_percentile(level):
    if level == "Untrained":
        return "1"
    elif level == "Beginner":
        return "5"
    elif level == "Novice":
        return "20"
    elif level == "Intermediate":
        return "50"
    elif level == "Advanced":
        return "80"
    elif level == "Elite":
        return "95"
    else:
        raise ValueError("Invalid Input!")

def calc_all_weight_standards(exerciseType, sex, body_weight, lift_weight):
    validateInputs(exerciseType, sex, body_weight, lift_weight)

    # Calculate competition scores
    wilks = calc_wilks_score(sex, body_weight, lift_weight)
    wilks2 = calc_wilks2_score(sex, body_weight, lift_weight)
    dots = calc_dots_score(sex, body_weight, lift_weight)

    # Calculate skill level stats
    bw_ratio = calc_body_weight_ratio(body_weight, lift_weight)

    if exerciseType == "Bench Press":
        level = calc_bench_level(sex, body_weight, lift_weight)
    elif exerciseType == "Squat":
        level = calc_squat_level(sex, body_weight, lift_weight)
    else:
        level = calc_deadlift_level(sex, body_weight, lift_weight)

    percentile = get_percentile(level)

    data = {
        "wilksScore": wilks,
        "wilks2Score": wilks2,
        "dotsScore": dots,
        "bwRatio": bw_ratio,
        "skillLevel": level,
        "percentile": percentile,
    }

    return json.dumps(data)
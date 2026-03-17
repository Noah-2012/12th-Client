import os
from collections import defaultdict

def count_java_code_lines(directory):
    file_stats = {}  # Datei -> Codezeilen

    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java"):
                file_path = os.path.join(root, file)
                lines = count_code_lines_in_file(file_path)
                file_stats[file_path] = lines

    return file_stats

def count_code_lines_in_file(file_path):
    code_lines = 0
    in_block_comment = False

    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            stripped_line = line.strip()

            if stripped_line.startswith("/*"):
                in_block_comment = True

            if in_block_comment:
                if "*/" in stripped_line:
                    in_block_comment = False
                continue

            if stripped_line.startswith("//") or not stripped_line:
                continue

            code_lines += 1

    return code_lines

def print_stats(file_stats):
    if not file_stats:
        print("Keine Java-Dateien gefunden.")
        return

    total_lines = sum(file_stats.values())
    total_files = len(file_stats)
    average_lines = total_lines / total_files

    max_file = max(file_stats, key=file_stats.get)
    max_lines = file_stats[max_file]

    min_file = min(file_stats, key=file_stats.get)
    min_lines = file_stats[min_file]

    print(f"Gefundene Java-Dateien: {total_files}")
    print(f"Gesamtzahl der Codezeilen: {total_lines}")
    print(f"Durchschnittliche Codezeilen pro Datei: {average_lines:.2f}")
    print(f"Datei mit den meisten Zeilen: {max_file} ({max_lines} Zeilen)")
    print(f"Datei mit den wenigsten Zeilen: {min_file} ({min_lines} Zeilen)")

    print("\nCodezeilen pro Package/Unterverzeichnis:")
    package_stats = defaultdict(int)
    for file_path, lines in file_stats.items():
        # Package = Verzeichnis relativ zum Startverzeichnis
        package = os.path.dirname(file_path)
        package_stats[package] += lines

    for package, lines in sorted(package_stats.items(), key=lambda x: x[1], reverse=True):
        print(f"{lines:5} Zeilen -> {package}")

    print("\nHistogramm der Dateigrößen:")
    histogram_bins = defaultdict(int)
    for lines in file_stats.values():
        if lines <= 50:
            histogram_bins['0-50'] += 1
        elif lines <= 100:
            histogram_bins['51-100'] += 1
        elif lines <= 200:
            histogram_bins['101-200'] += 1
        elif lines <= 300:
            histogram_bins['201-300'] += 1
        elif lines <= 400:
            histogram_bins['301-400'] += 1
        elif lines <= 500:
            histogram_bins['401-500'] += 1
        else:
            histogram_bins['501+'] += 1

    for bin_range, count in sorted(histogram_bins.items()):
        print(f"{bin_range:7} : {count} Dateien")

if __name__ == "__main__":
    directory = "."  # aktuelles Verzeichnis
    file_stats = count_java_code_lines(directory)
    print_stats(file_stats)

import xml.etree.ElementTree as ET
import os
from datetime import datetime
import argparse

SUCCESS_ICON = ":white_check_mark:"
FAIL_ICON = ":x:"

class JacocoReport:
    def __init__(self, xml_path):
        self.xml_path = xml_path
        self.tree = ET.parse(xml_path)
        self.root = self.tree.getroot()

    def get_coverage_data(self):
        """Extract coverage data from XML"""
        packages = []
        total_stats = {
            'class': {'covered': 0, 'total': 0},
            'method': {'covered': 0, 'total': 0},
            'line': {'covered': 0, 'total': 0},
            'instruction': {'covered': 0, 'total': 0}
        }

        for package in self.root.iter('package'):
            #print(f"Package...")
            pkg_name = package.attrib['name']
            pkg_stats = {
                'name': pkg_name,
                'class': {'covered': 0, 'total': 0},
                'method': {'covered': 0, 'total': 0},
                'line': {'covered': 0, 'total': 0},
                'instruction': {'covered': 0, 'total': 0}
            }

            # Process Counters per Package
            for counter in package.iter('counter'):
                attr_type = counter.attrib['type']
                attr_type = attr_type.lower()
                attr_missed = counter.attrib['missed']
                attr_covered = counter.attrib['covered']

                if attr_type in pkg_stats:
                    pkg_stats[attr_type]['total'] = int(attr_missed) + int(attr_covered)
                    pkg_stats[attr_type]['covered'] = int(attr_covered)


            # Update total statistics
            for metric in ['class', 'method', 'line', 'instruction']:
                total_stats[metric]['covered'] += pkg_stats[metric]['covered']
                total_stats[metric]['total'] += pkg_stats[metric]['total']

            packages.append(pkg_stats)

        return packages, total_stats

    def generate_markdown(self, output_path, show_details: bool = True, check_coverage: float = 85.0):
        """Generate Markdown report"""
        packages, total_stats = self.get_coverage_data()

        success_rate_emoji = SUCCESS_ICON

        # Calculate overall coverage percentages
        class_coverage = (total_stats['class']['covered'] / total_stats['class']['total'] * 100) if total_stats['class']['total'] > 0 else 0
        method_coverage = (total_stats['method']['covered'] / total_stats['method']['total'] * 100) if total_stats['method']['total'] > 0 else 0
        line_coverage = (total_stats['line']['covered'] / total_stats['line']['total'] * 100) if total_stats['line']['total'] > 0 else 0
        instruction_coverage = (total_stats['instruction']['covered'] / total_stats['instruction']['total'] * 100) if total_stats['instruction']['total'] > 0 else 0

        if instruction_coverage < check_coverage:
            success_rate_emoji = FAIL_ICON

        # Generate report content
        report = [
            f"# JaCoCo Code Coverage Report &nbsp;&nbsp; {success_rate_emoji}",
            f"\nGenerated on: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",

            "\n## Overall Coverage",
            f"\n- **Instructions**: &nbsp;&nbsp; {total_stats['instruction']['covered']} of {total_stats['instruction']['total']} ({instruction_coverage:.1f}%)",
            f"- **Classes**: &nbsp;&nbsp; {total_stats['class']['covered']} of {total_stats['class']['total']} ({class_coverage:.1f}%)",
            f"- **Methods**: &nbsp;&nbsp; {total_stats['method']['covered']} of {total_stats['method']['total']} ({method_coverage:.1f}%)",
            f"- **Lines**: &nbsp;&nbsp; {total_stats['line']['covered']} of {total_stats['line']['total']} ({line_coverage:.1f}%)"

        ]

        if show_details:
            header = [
                        "\n## Coverage by Package",
                        "\n| Package | Instruction Coverage | Class Coverage | Method Coverage | Line Coverage |",
                        "| --- | --- | --- | --- | --- |"
            ]
            report += header

            # Add package details
            for pkg in packages:
                pkg_class_coverage = (pkg['class']['covered'] / pkg['class']['total'] * 100) if pkg['class']['total'] > 0 else 0
                pkg_method_coverage = (pkg['method']['covered'] / pkg['method']['total'] * 100) if pkg['method']['total'] > 0 else 0
                pkg_line_coverage = (pkg['line']['covered'] / pkg['line']['total'] * 100) if pkg['line']['total'] > 0 else 0
                pkg_instruction_coverage = (pkg['instruction']['covered'] / pkg['instruction']['total'] * 100) if pkg['instruction']['total'] > 0 else 0

                report.append(
                    f"| `{pkg['name']}` | "
                    f"{pkg['instruction']['covered']}/{pkg['instruction']['total']} ({pkg_instruction_coverage:.1f}%) | "
                    f"{pkg['class']['covered']}/{pkg['class']['total']} ({pkg_class_coverage:.1f}%) | "
                    f"{pkg['method']['covered']}/{pkg['method']['total']} ({pkg_method_coverage:.1f}%) | "
                    f"{pkg['line']['covered']}/{pkg['line']['total']} ({pkg_line_coverage:.1f}%) |"
                )

        # Write to file
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write('\n'.join(report))

def main():
    parser = argparse.ArgumentParser(description='Generate JaCoCo report')
    parser.add_argument('--input','-i',
                      default='build/reports/jacoco/jacocoMergedReport/jacocoTestReport.xml',
                      help='List of files to process, separated by comma or semicolon')
    parser.add_argument('--details', '-d',
                      action='store_true',
                      help='Include detailed coverage by class')
    parser.add_argument('--output', '-o',
                      default='jacoco-report.md',
                      help='Output file path (default: jacoco-report.md)')
    parser.add_argument('--check', '-c',
                      default='80.0',
                      help='Check coverage equals to input (default: 80.0%)')

    args = parser.parse_args()

    report = JacocoReport(args.input)
    report.generate_markdown(args.output, args.details, float(args.check))

    print(f"Report successfully generated at: {args.output}")

if __name__ == "__main__":
    main()
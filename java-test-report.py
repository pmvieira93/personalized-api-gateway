import xml.etree.ElementTree as ET
import os
from collections import defaultdict
from typing import Dict, List
import argparse

SUCCESS_ICON = ":white_check_mark:"
FAIL_ICON = ":x:"
DEFAULT_FOLDERS = "build/test-results/functionalTest;build/test-results/integrationTest;build/test-results/test"


class TestResults:
    def __init__(self):
        self.tests = 0
        self.failures = 0
        self.errors = 0
        self.skipped = 0
        self.class_results = defaultdict(lambda: {
            'tests': 0,
            'failures': 0,
            'skipped': 0,
            'failure_details': []
        })

class JUnitReportGenerator:
    def __init__(self):
        self.folder_results: Dict[str, TestResults] = {}

    def parse_xml_file(self, file_path: str, results: TestResults):
        tree = ET.parse(file_path)
        root = tree.getroot()

        for testsuite in root.iter('testsuite'):
            class_name = testsuite.get('name')

            # Check if have testcase
            if testsuite.find('testcase'):

                # Update class-level statistics
                results.class_results[class_name]['tests'] += int(testsuite.get('tests', 0))
                results.class_results[class_name]['failures'] += int(testsuite.get('failures', 0))
                results.class_results[class_name]['skipped'] += int(testsuite.get('skipped', 0))

                # Process individual test cases
                for testcase in testsuite.iter('testcase'):
                    failure = testcase.find('failure')
                    skipped = testcase.find('skipped')

                    if failure is not None:
                        results.class_results[class_name]['failure_details'].append({
                            'test': testcase.get('name'),
                            'type': 'Failure',
                            'error_type': failure.get('type'),
                            'message': failure.get('message'),
                            'details': failure.text
                        })

    def calculate_overall_totals(self):
        totals = {
            'tests': 0,
            'failures': 0,
            'skipped': 0
        }
        for results in self.folder_results.values():
            totals['tests'] += results.tests
            totals['failures'] += results.failures
            totals['skipped'] += results.skipped
        return totals

    def process_folders(self, folders: List[str]):
        for folder in folders:
            if not os.path.exists(folder):
                print(f"Warning: Folder {folder} does not exist. Skipping...")
                continue

            results = TestResults()
            for file in os.listdir(folder):
                if file.endswith('.xml'):
                    self.parse_xml_file(os.path.join(folder, file), results)

            # Calculate folder totals
            for class_results in results.class_results.values():
                results.tests += class_results['tests']
                results.failures += class_results['failures']
                results.skipped += class_results['skipped']

            self.folder_results[folder] = results

    def generate_markdown_report(self, show_details: bool = True):
        report = []

        # Overall Summary across all folders
        totals = self.calculate_overall_totals()

        successful = totals['tests'] - totals['failures'] - totals['skipped']
        success_rate = (successful / totals['tests'] * 100) if totals['tests'] > 0 else 0
        success_rate_emoji = SUCCESS_ICON

        if success_rate < 100 :
            success_rate_emoji = FAIL_ICON

        report.append(f"# Test Execution Report &nbsp;&nbsp; {success_rate_emoji}\n")

        report.append(f"## Overall Summary &nbsp;&nbsp;&nbsp;&nbsp; {success_rate:.2f}%")
        report.append(f"Total Tests: {totals['tests']}, Successful: {successful}, Failures: {totals['failures']}, Skipped: {totals['skipped']}")

        # Summary by Folder
        report.append("\n## Summary by Folder")
        report.append("\n| Folder | Total Tests | Successful | Failures | Skipped | Success Rate |")
        report.append("|---------|-------------|------------|----------|----------|--------------|")

        for folder, results in self.folder_results.items():
            successful = results.tests - results.failures - results.skipped
            success_rate = (successful / results.tests * 100) if results.tests > 0 else 0
            folder_name = os.path.basename(folder)
            report.append(f"| {folder_name} | {results.tests} | {successful} | "
                        f"{results.failures} | {results.skipped} | "
                        f"{success_rate:.2f}% |")

        if show_details:

            # Detailed Results by Folder
            for folder, results in self.folder_results.items():
                folder_name = os.path.basename(folder)

                report.append(f"\n## Details of: {folder_name}")

                # Class-level Results Table
                report.append("\n| Class Name | Total Tests | Successful | Failures | Skipped | Success Rate |")
                report.append("|------------|-------------|------------|----------|----------|--------------|")

                for class_name, class_results in results.class_results.items():
                    successful = class_results['tests'] - class_results['failures'] - class_results['skipped']
                    success_rate = (successful / class_results['tests'] * 100) if class_results['tests'] > 0 else 0

                    report.append(f"| {class_name} | {class_results['tests']} | {successful} | "
                                f"{class_results['failures']} | "
                                f"{class_results['skipped']} | {success_rate:.2f}% |")

                # Failure Details in Collapsible Sections
                if any(len(class_results['failure_details']) > 0 for class_results in results.class_results.values()):
                    report.append("\n### Test Failures & Errors")

                    for class_name, class_results in results.class_results.items():
                        if class_results['failure_details']:
                            report.append(f"\n<details>")
                            report.append(f"<summary><strong>{class_name} - Failed Tests</strong></summary>\n")

                            for detail in class_results['failure_details']:
                                report.append(f"#### _{detail['test']}_")
                                report.append(f"**Type**: `{detail['type']} ({detail['error_type']})`")
                                report.append(f"**Message**: `{detail['message']}`\n")
                                if detail['details']:
                                    report.append("**Stack Trace**:")
                                    report.append("```")
                                    report.append(detail['details'].strip())
                                    report.append("```")
                                report.append("\n---\n")

                            report.append("</details>")

        return "\n".join(report)


def parse_folder_list(folder_string: str) -> List[str]:
    """Parse folder string using either comma or semicolon as separator"""
    if ',' in folder_string:
        return [folder.strip() for folder in folder_string.split(',')]
    return [folder.strip() for folder in folder_string.split(';')]


def main():
    parser = argparse.ArgumentParser(description='Generate JUnit test report from multiple folders')
    parser.add_argument('--input','-i',
                      default=DEFAULT_FOLDERS,
                      help='List of folders to process, separated by comma or semicolon')
    parser.add_argument('--details', '-d',
                      action='store_true',
                      help='Include detailed test results and failure information')
    parser.add_argument('--output', '-o',
                      default='test-report.md',
                      help='Output file path (default: test-report.md)')

    args = parser.parse_args()

    # Parse folders list
    folders = parse_folder_list(args.input)

    generator = JUnitReportGenerator()
    generator.process_folders(folders)

    report = generator.generate_markdown_report(show_details=args.details)

    # Save report
    with open(args.output, "w", encoding="utf-8") as f:
        f.write(report)

    print(f"Report generated successfully: {args.output}")
    print(f"Processed folders: {', '.join(folders)}")

if __name__ == "__main__":
    main()
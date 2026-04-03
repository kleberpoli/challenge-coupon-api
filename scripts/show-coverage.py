#!/usr/bin/env python3
"""
show-coverage.py
----------------
Reads JaCoCo XML report and prints total instruction coverage in a clean, professional format.
"""

# Standard library imports
import sys                         # For exiting with error codes
import xml.etree.ElementTree as ET # For parsing XML
from pathlib import Path           # For handling file paths in a cross-platform way

# Path to JaCoCo XML report
JACOCO_XML = Path("target/site/jacoco/jacoco.xml")

def main():
    # Check if the report exists
    if not JACOCO_XML.exists():
        print("❌ Coverage report not found. Run 'mvn verify jacoco:report' first.")
        sys.exit(1)

    # Parse XML
    tree = ET.parse(JACOCO_XML)
    root = tree.getroot()

    # Find instruction counter
    for counter in root.findall("counter"):
        if counter.attrib.get("type") == "INSTRUCTION":
            missed = int(counter.attrib.get("missed", 0))
            covered = int(counter.attrib.get("covered", 0))
            total = missed + covered
            pct = (covered / total * 100) if total > 0 else 0.0

            # Print coverage in a clean format
            print("──────────────────────────────")
            print("       JaCoCo Coverage        ")
            print("──────────────────────────────")
            print(f" Total Instructions: {total:5}")
            print(f" Covered:            {covered:5}")
            print(f" Missed:             {missed:5}")
            print(f" Coverage:           {pct:5.1f}%")
            print("──────────────────────────────")
            return

    # If no instruction counter found
    print("⚠️ Instruction counter not found in jacoco.xml")
    sys.exit(1)


if __name__ == "__main__":
    main()
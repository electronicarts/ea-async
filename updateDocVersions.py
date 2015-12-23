#!/usr/bin/python

import os
import re
import sys

if len(sys.argv) != 2:
    u = "Usage: updateDocVersions <new-version>\n"
    sys.stderr.write(u)
    sys.exit(1)

version = sys.argv[1]

pat1 = [re.compile(r"<version>[^<]*</version>", re.MULTILINE), r"<version>" + version + "</version>", (".md")]
pat2 = [re.compile(r"ea-async-[.0-9a-z_-]+[.]jar", re.MULTILINE), r"ea-async-" + version + ".jar", (".md")]
pat3 = [re.compile(r"(<artifactId>ea-async(-maven-plugin)?</artifactId>\s*<version)>[^<]*(</version>)"),
        r"\1>" + version + r"\3", ("project-to-test/pom.xml")]

patterns = [pat1, pat2, pat3]
root = "."
extensions = (".md", ".xml")

for dir, subdirs, names in os.walk(root):
    for name in names:
        path = os.path.join(dir, name)
        nameLower = path.lower().replace("\\", "/")
        if nameLower.endswith(extensions):
            text = open(path).read()
            modifiedText = text
            for pattern in patterns:
                if nameLower.endswith(pattern[2]):
                    modifiedText = re.sub(pattern[0], pattern[1], modifiedText)
            if modifiedText != text:
                print path
                open(path, 'w').write(modifiedText)

import sys
import unicodedata
from io import TextIOWrapper

fileName = sys.argv[1]
outputFile = sys.argv[2]
user = sys.argv[3]
i = 0
velocities = []
rotations = []
leftEncoderPositions = []
rightEncoderPositions = []
bad_chars = ['ï»¿']

with open(fileName) as f, open(outputFile, "w") as out:
    fileName.encode('ascii', 'remove')
    f_str = f.name
    for a in bad_chars :
           f_str = f_str.replace(a, '')
    g = TextIOWrapper(f_str)
    for line in g.readlines():
        if line.startswith("ï»¿") or line.startswith(" ") or line[0].isdigit():
            for entry in line.strip().split(" "):
                try:
                    float(entry)
                except:
                    print(entry)
                    continue
                if i == 0:
                    velocities.append(entry)
                if i == 1:
                    rotations.append(entry)
                if i == 2:
                    leftEncoderPositions.append(entry)
                if i == 4:
                    rightEncoderPositions.append(entry)

                out.write(entry + " ")
                i += 1
                if i == 6:
                    i = 0
                    out.write("\n")
        else:
            print(line)

velocityArray = ",\n\t\t".join(velocities)
rotationsArray = ",\n\t\t".join(rotations)
leftEncoderPositionArray = ",\n\t\t".join(leftEncoderPositions)
rightEncoderPositionArray = ",\n\t\t".join(rightEncoderPositions)


content = """
/*
PATH HAS BEEN AUTO-GENERATED BY THE PYTHON PARSER. DO NOT TOUCH OR YOU WILL HAVE BAD LUCK FOR 100 YEARS.
*/
package frc.robot;

public class %s {
    public double[] generated_leftEncoderPositions = {
        %s
    };
    public double[] generated_rightEncoderPositions = {
        %s
    };

}

""" % (
    fileName,
    # velocityArray,
    # rotationsArray,
    leftEncoderPositionArray,
    rightEncoderPositionArray,
)

with open("C:/Users/" + user + "/Documents/GitHub/2020-2021-InfiniteRecharge/src/main/java/frc/robot/" + fileName + ".java", "w") as f:
    f.write(content)

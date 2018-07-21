# QuasiTiler
This a modification to the QuasiTiler v0.2 by Eugenio Durand, the original source code on which this project is built can be download from [here](https://sourceforge.net/projects/quasitiler/). This project is licensed under the terms of the GNU General Public License.

How to run it:
1. Compile the source code by executing __compile.cmd__ (for Windows) or __compile.sh__ (for Mac/Linus). (You only need to do it once.)
2. Run the program by executing __run.cmd__ (for Windows) or __run.sh__ (for Mac/Linus).

For an excellent explanation about the how the program works, please consult an [article](http://www.geom.uiuc.edu/apps/quasitiler/about.html) written by the original author, _Eugenio Durand_, of this program.

Modifications made:
- All floats are replaced by doubles to increase precision. 
- An __export/__ folder will be created at the first run-time. The coordinates of the included vertices, the projected vertices and the "generator" are exported into the text files __vertices.txt__, __tiles.txt__ and __generator.txt__ respectively.

What's inside each export text files?

__vertices.txt__: Each line contains the coordinates of an included vertex in the ambient space (or superspace), separated by a tab ('\t'). 

__tiles.txt__:  Each line contains the 2D coordinates of an included vertex __projected__ onto the projection space, separated by a tab ('\t'). 

__generator.txt__:  A generator is a set of two vectors defining the direction (or orientation) of the projection space. There are two lines in the file. Each line contains the coordinates of a vector in the generator in the ambient space (or superspace), separated by a tab ('\t'). 

(numbers containing decimal places are shown in 100 deciaml places to ensure no loss of precision)

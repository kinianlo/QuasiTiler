# QuasiTiler
This a modification to the QuasiTiler v0.2 by Eugenio Durand. The original source code, on which this project is built, can be downloaded from [here](https://sourceforge.net/projects/quasitiler/). This project is licensed under the terms of the GNU General Public License.

How to run it:
1. Compile the source code by executing __compile.cmd__ (for Windows) or __compile.sh__ (for Mac/Linux). (You only need to do it once.)
2. Run the program by executing __run.cmd__ (for Windows) or __run.sh__ (for Mac/Linux).

For an excellent explanation about the how the program works, please consult an [article](http://www.geom.uiuc.edu/apps/quasitiler/about.html) written by the original author, _Eugenio Durand_, of this program.

__Modifications made:__
- All floats are replaced by doubles to increase precision. 
- Greatly improved rendering speed.
- Allow an option for displaying the centroid of tiles. 
- An __export/__ folder will be created at the first run-time. The coordinates of the included vertices, the projected vertices, the "generator" and the offset vector are exported into the text files __vertices.txt__, __tiles.txt__, __generator.txt__ and __offset.txt__ respectively. The contents in the export files will be updated every time the tiling in the program is updated.

__What's inside each export text file?__

__tiles.txt__: Each line contains the information a tile on the projection plane. The first column provides the __shape__ of the tile and the second column column provides the __orientation__ of the tile.  The last two columns contains the coordinates of the centroid of the tile.

__vertices.txt__: Each line contains the coordinates of an included vertex in the ambient space (or superspace), separated by a tab ('\t'). 

__projected_vertices.txt__:  Each line contains the 2D coordinates of an included vertex __projected__ onto the projection plane, separated by a tab ('\t'). 

__generator.txt__:  _A generator is a set of two vectors defining the direction (or orientation) of the projection plane._ There are two lines in the file. Each line contains the coordinates of a vector in the generator in the ambient space, separated by a tab ('\t'). 

__offset.txt__: This file has only one line which contains the coordinates of the offset vector, i.e. a point in ambient space at which the project plane goes through. Oddly enough, the original program only allows the offset to be within the first two dimensions of the ambient space. So there are always two coordinates in this file and the rest should be treated as zeros.



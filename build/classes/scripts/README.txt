INCAR is a suite of few python and perl scripts which can be executed one after another in a specified manner to get different levels of output i.e all hydrogen bonded interactions, base pairs, bifurcated base pairs and triples and also an ordered output where all nucleotides and their interactions with other nucleotides are listed and sorted according to residue order.
INCAR can not take pdb file directly as input. H-atoms are added to a pdbfile using VMD and then after fixing the heavy atoms Hydrogen positions are optimized using NAMD. Now one pdb-min.coor file is generated which is the input file for INCAR.
Here I am giving some example pdb-min.coor files to execute the codes to generate output files.
step 1:
python incarfinal28-7.py pdb-min.coor
one output file "pdb-min.coor.out" will generated which contains all h-bonding information
step 2:
python allbiffinal.py pdb-min.coor pdb-min.coor.out>pdb-bp.out
In this step one "pdb-min.coor.out.all" file genarated and all information about basepairing (including single bonded basepairs),bifurcated base pairing, bifurcated triples ion and water mediated interactions is stored in "pdb-bp.out" file. If results are not redirected in any output file, then all outputs will be displayed in terminal. But as this output is required to run ordered.pl script. so it is better to save the output in a file.
step 3:
perl ordered.pl pdb-min.coor pdb-bp.out
It will asks for whether user wants to include single hydrogen bonded pairs also or not.
Then output will be displayed in the terminal. But user can redirect the result to any output file if he/she wants.

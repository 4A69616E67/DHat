==================DLO-HiC Analysis Tools====================

Usage: java -jar DLO-HIC-AnalysisTools.jar -conf <ConfigFile> [options]

==========================require==================================
jre                 >= 1.8
mafft               need add in "PATH"
bwa                 need add in "PATH"
python              2.XX recommend 2.7
python module:
matplotlib          install by "pip install matplotlib"
scipy               install by "pip install scipy"

recommend install "AnaConda" and then you can install all of above tools by "conda install XXXX" (exclude python)
//mafft and bwa can install by "conda install bwa/mafft" (if you install AnaConda before)
//if no use please try "conda install -c bioconda  mafft/bwa"

=====================ConfigFile: Such as follow====================

#------------------------------required parameters----------------------------
InputFile = DLO-test.fastq
Restriction = T^TAA
HalfLinker = GTCGGAGAACCAGTAGCT
GenomeFile = Hg19.clean.fna
#------------------------------optional parameters---------------------------
OutPath = ./
Prefix = out
Index = Hg19
Chromosomes =
AdapterSeq =
Resolutions = 1000000
DrawResolutions = 1000000
Thread = 4
Step = -
#------------------------------advance parameters---------------------------
MatchScore = 1
MisMatchScore = -1
InDelScore = -1
MinLinkerLen =
MinReadsLength = 16
MaxReadsLength = 20
AlignThread = 1
AlignType = Short
AlignMisMatch = 0
MinUniqueScore =
Iteration = true
DeBugLevel = 0

=================================================================================

InputFile           String      Input File with Fastq Format
Restriction         String      Sequence of restriction, enzyme cutting site expressed by "^"
HalfLinker          String[]    Halflinker sequences (different half-linker separated with a space)
GenomeFile          String      Reference genome file
#===============================================================================
OutPath             String      Path of output  (default    "./")
Prefix              String      prefix of output    (default    "DLO_Out")
Index               String      Index prefix of reference genome
Chromosomes         String[]    Chromosome name must same as Chromosome name in reference genome    (default all in reference genome)
AdapterSeq          String[]    Adapter sequence, null means don't remove adapter   (default    "")
                                If you want to remove adapter but you don't know the adapter seq, you can set "Auto"
Resolutions         Int[]       Bin size when create interaction matrix  (default    "1000000" byte)
DrawResolution      Int[]       Resolution for you draw heat-map    (default    "100000")
Thread              Int         Number of threads    (default    "4")
Step                String[]    assign  where start and end (default    "-")
#===============================================================================
MatchScore          Int         Match score in linker filter    (default    "1")
MisMatchScore       Int         MisMatch Score in linker filter (default    "-1")
InDelScore          Int         Indel Score in linker filter    (default    "-1")
MinLinkerLen        Int         Minimum linker length
MinReadsLength      Int         Min reads length when extract interaction reads (default    "16")
MaxReadsLength      Int         Max reads length when extract interaction reads (default    "20")
AlignThread         Int         Threads in alignment (default    "2")
AlignType           String      Reads type include ["Short","Long"] (default    "Short")
AlignMisMatch       Int         MisMatch number in alignment    (default    "0")
MinUniqueScore      Int         Minimum mapQ what reads mapQ less than it will be removed
Iteration           Bool      "true" or "false" represent whether do iteration alignment
DeBugLevel          Int         0 means remain base output, 1 means more output, 2 means all output (default    "0")


//if we set ReadsType "Short", we will align with "bwa aln",and if set "Long",we will align with "bwa mem"

//Step include "PreProcess" "Alignment" "Bed2BedPe" "NoiseReduce" "BedPe2Inter" "MakeMatrix"
//If we want to run from "Bed2BedPe" to "MakeMatrix", we can set "Bed2BedPe - MakeMatrix"
//If we only want to run from "Alignment" to end, we can set "SeProcess -"
//If we want to run all, we can set "-"

#=========================Other Script=================
java -cp DLO-HIC-AnalysisTools.jar Utils.CreateMatrix
java -cp DLO-HIC-AnalysisTools.jar Bin.Guide    (need visual interface)
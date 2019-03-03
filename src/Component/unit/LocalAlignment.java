package Component.unit;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;

public class LocalAlignment {

    private int MatchScore;
    private int MismatchScore;
    private int IndelScore;
    private char[] Seq1 = new char[]{};
    private char[] Seq2 = new char[]{};
    private int[][] ScoreMatrix;
    private int MaxScore;
    private int[] MaxIndex;
    private int[] MinIndex;
    private int[] MatrixSzie;
    // ****** assumptions ******
    // 1) the aligned strings are from the beginning of the original sequences
    // 2) the aligned str2 is the linker sequence
    // 3) the insertions and deletions are represented with '-'

    public LocalAlignment(int matchscore, int mismatchscore, int indelscore) {
        MatchScore = matchscore;
        MismatchScore = mismatchscore;
        IndelScore = indelscore;
        Init();
//        MinSeqLength = minseqlength;
    }

    public LocalAlignment() {
        MatchScore = Default.MatchScore;
        MismatchScore = Default.MisMatchScore;
        IndelScore = Default.InDelScore;
        Init();
    }

    private void Init() {
        MatrixSzie = new int[]{Seq1.length + 1, Seq2.length + 1};
        ScoreMatrix = new int[MatrixSzie[0]][MatrixSzie[1]];
        MaxIndex = new int[]{Seq1.length, Seq2.length};
        MinIndex = MaxIndex.clone();
        InitMatrix();
    }

    private void InitMatrix(int score) {
        for (int[] aScoreMatrix : ScoreMatrix) {
            Arrays.fill(aScoreMatrix, score);
        }
    }

    private void InitMatrix() {
        InitMatrix(0);
    }

    public void FindMaxIndex() {
        MaxScore = 0;
        MaxIndex = new int[]{0, 0};
        for (int i = 0; i < MatrixSzie[0]; i++) {
            for (int j = 0; j < MatrixSzie[1]; j++) {
                if (ScoreMatrix[i][j] > MaxScore) {
                    MaxScore = ScoreMatrix[i][j];
                    MaxIndex[0] = i;
                    MaxIndex[1] = j;
                }
            }
        }
    }

    public void FindMinIndex() {
        int MinI = MaxIndex[0];
        int MinJ = MaxIndex[1];
        while (MinI > 0 && MinJ > 0) {
            if (Seq1[MinI - 1] == Seq2[MinJ - 1] && ScoreMatrix[MinI][MinJ] == ScoreMatrix[MinI - 1][MinJ - 1] + MatchScore) {
                MinI--;
                MinJ--;
//                if (ScoreMatrix[MinI][MinJ] <= 0) {
//                    MinI++;
//                    MinJ++;
//                    break;
//                }
            } else if (ScoreMatrix[MinI][MinJ] == ScoreMatrix[MinI - 1][MinJ - 1] + MismatchScore) {
                MinI--;
                MinJ--;
//                if (ScoreMatrix[MinI][MinJ] <= 0) {
//                    MinI++;
//                    MinJ++;
//                    break;
//                }
            } else if (ScoreMatrix[MinI][MinJ] == ScoreMatrix[MinI - 1][MinJ] + IndelScore) {
                MinI--;
//                if (ScoreMatrix[MinI][MinJ] <= 0) {
//                    MinI++;
//                    break;
//                }
            } else if (ScoreMatrix[MinI][MinJ] == ScoreMatrix[MinI][MinJ - 1] + IndelScore) {
                MinJ--;
//                if (ScoreMatrix[MinI][MinJ] <= 0) {
//                    MinJ++;
//                    break;
//                }
            } else {
                MinI--;
                MinJ--;
            }
        }
        MinI++;
        MinJ++;
        MinIndex = new int[]{MinI, MinJ};
    }

    public void CreateMatrix(String seq1, String seq2) {
        if (MatrixSzie[0] < seq1.length() + 1 || MatrixSzie[1] < seq2.length() + 1) {
            ScoreMatrix = new int[seq1.length() + 1][seq2.length() + 1];
        }
        MatrixSzie = new int[]{seq1.length() + 1, seq2.length() + 1};
        Seq1 = seq1.toCharArray();
        Seq2 = seq2.toCharArray();
        int Seq1Length = Seq1.length;
        int Seq2Length = Seq2.length;
        int Seq1Index, Seq2Index;
        for (Seq1Index = 0; Seq1Index < Seq1Length; Seq1Index++) {
            for (Seq2Index = 0; Seq2Index < Seq2Length; Seq2Index++) {
                if (Seq1[Seq1Index] == Seq2[Seq2Index]) {
                    ScoreMatrix[Seq1Index + 1][Seq2Index + 1] = ScoreMatrix[Seq1Index][Seq2Index] + MatchScore;
                } else {
                    ScoreMatrix[Seq1Index + 1][Seq2Index + 1] = ScoreMatrix[Seq1Index][Seq2Index] + MismatchScore;
                }
                if (ScoreMatrix[Seq1Index + 1][Seq2Index + 1] < ScoreMatrix[Seq1Index + 1][Seq2Index] + IndelScore) {
                    ScoreMatrix[Seq1Index + 1][Seq2Index + 1] = ScoreMatrix[Seq1Index + 1][Seq2Index] + IndelScore;
                }
                if (ScoreMatrix[Seq1Index + 1][Seq2Index + 1] < ScoreMatrix[Seq1Index][Seq2Index + 1] + IndelScore) {
                    ScoreMatrix[Seq1Index + 1][Seq2Index + 1] = ScoreMatrix[Seq1Index][Seq2Index + 1] + IndelScore;
                }
                if (ScoreMatrix[Seq1Index + 1][Seq2Index + 1] < 0) {
                    ScoreMatrix[Seq1Index + 1][Seq2Index + 1] = 0;
                }
            }
        }
    }
    // trace back to get the maximum aligned sub-sequences
//        alignedStr1.delete(0, alignedStr1.length());
//        alignedStr2.delete(0, alignedStr2.length());
//        alignedStatus.delete(0, alignedStatus.length());
//    SeqIndex =MaxI -1;
//    LinkerIndex =MaxJ -1;
//
//    int ScoreGreaterThan0 = 1;
//        while((SeqIndex >=0)||(LinkerIndex >=0))
//
//    {
//        if (SeqIndex < 0) { // deletion at the beginning of seq, insertion in linker
//            alignedStr1.append('-');
//            alignedStr2.append(linker.charAt(LinkerIndex));
//            alignedStatus.append(' ');
//            LinkerIndex--;
//        } else if (LinkerIndex < 0) { // insertion in seq, deletion at the beginning of linker
//            alignedStr1.append(seq.charAt(SeqIndex));
//            alignedStr2.append('-');
//            alignedStatus.append(' ');
//            SeqIndex--;
//        } else if ((ScoreMatrix[SeqIndex + 1][LinkerIndex + 1] == ScoreMatrix[SeqIndex][LinkerIndex] + MatchScore) && (seq.charAt(SeqIndex) == linker.charAt(LinkerIndex))) {
//            // match from both strs
//            alignedStr1.append(seq.charAt(SeqIndex));
//            alignedStr2.append(linker.charAt(LinkerIndex));
//            alignedStatus.append('|');
//            SeqIndex--;
//            LinkerIndex--;
//        } else if (ScoreMatrix[SeqIndex + 1][LinkerIndex + 1] == ScoreMatrix[SeqIndex][LinkerIndex] + MismatchScore) {
//            // mismatch from both strs
//            alignedStr1.append(seq.charAt(SeqIndex));
//            alignedStr2.append(linker.charAt(LinkerIndex));
//            alignedStatus.append('X');
//            SeqIndex--;
//            LinkerIndex--;
//        } else if (ScoreMatrix[SeqIndex + 1][LinkerIndex + 1] == ScoreMatrix[SeqIndex + 1][LinkerIndex] + IndelScore) {
//            // deletion in seq, insertion in linker
//            alignedStr1.append('-');
//            alignedStr2.append(linker.charAt(LinkerIndex));
//            alignedStatus.append(' ');
//            LinkerIndex--;
//        } else {
//            // insertion in seq, deletion in linker
//            //if (ScoreMatrix[SeqIndex + 1][LinkerIndex + 1] == ScoreMatrix[SeqIndex][LinkerIndex + 1] + IndelScore)
//            alignedStr1.append(seq.charAt(SeqIndex));
//            alignedStr2.append('-');
//            alignedStatus.append(' ');
//            SeqIndex--;
//        }
//        if (ScoreGreaterThan0 == 1) {
//            if (ScoreMatrix[SeqIndex + 1][LinkerIndex + 1] <= 0) {
//                MinI = SeqIndex + 1;
//                MinJ = LinkerIndex + 1;
//                ScoreGreaterThan0 = 0;
//            }
//        }
//        if (debugLevel > 2) {
//            System.out.println("SeqIndex = " + (SeqIndex + 1) + "; LinkerIndex = " + (LinkerIndex + 1));
//        }
//    }
//        alignedStr1.reverse();
//        alignedStr2.reverse();
//        alignedStatus.reverse();
//        if(debugLevel >2)
//
//    {
//        System.out.println("seq: " + seq);
//        System.out.println("linker: " + linker);
//        System.out.println("aligned Score: " + Score);
//        System.out.println("aligned seq      : " + getAlignedStr1());
//        System.out.println("aligned strStatus : " + getAlignedStatus());
//        System.out.println("aligned linker      : " + getAlignedStr2());
//    }

    public void PrintMatrix() {
        int index1, index2;
        int length1 = Seq1.length;
        int length2 = Seq2.length;
        System.out.println("length1 = " + length1);
        System.out.println("length2 = " + length2);

        System.out.print(" \t_");
        for (index2 = 1; index2 <= length2; index2++) {
            System.out.print("\t" + Seq2[index2 - 1]);
        }
        System.out.print("\n" + "_");
        for (int i = 0; i <= length2; i++) {
            System.out.print("\t" + ScoreMatrix[0][i]);
        }
        System.out.println();
        for (index1 = 1; index1 <= length1; index1++) {
            System.out.print(Seq1[index1 - 1]);
            for (index2 = 0; index2 <= length2; index2++) {
                System.out.print("\t" + ScoreMatrix[index1][index2]);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Component.unit.LocalAlignment <sequence 1> <sequence 2> [option]");
            System.exit(0);
        }
        if (args.length == 2) {
            LocalAlignment localAligner = new LocalAlignment();
            localAligner.CreateMatrix(args[0], args[1]);
            localAligner.FindMaxIndex();
            localAligner.FindMinIndex();
            System.out.println(String.join("\n", localAligner.PrintAlignment()));
//            localAligner.PrintMatrix();
            //localAligner.CreateMatrix("AACCGGTT", "ACCGTATT");
        } else {
            LocalAlignment localAligner = new LocalAlignment(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
            localAligner.CreateMatrix(args[0], args[1]);
            localAligner.FindMaxIndex();
            localAligner.FindMinIndex();
            System.out.println(String.join("\n", localAligner.PrintAlignment()));
//            localAligner.PrintMatrix();
        }
    }

    public int getMaxScore() {
        return MaxScore;
    }

    public int[] getMaxIndex() {
        return MaxIndex;
    }

    public int[] getMinIndex() {
        return MinIndex;
    }

    public String[] PrintAlignment() {
        String[] AlignStat = new String[]{"", "", ""};
        int i = MaxIndex[0];
        int j = MaxIndex[1];
        while (ScoreMatrix[i][j] > 0) {
            if (ScoreMatrix[i][j] == ScoreMatrix[i - 1][j - 1] + MatchScore && Seq1[i - 1] == Seq2[j - 1]) {
                AlignStat[0] = Seq1[i - 1] + AlignStat[0];
                AlignStat[1] = "|" + AlignStat[1];
                AlignStat[2] = Seq2[j - 1] + AlignStat[2];
                i--;
                j--;
            } else if (ScoreMatrix[i][j] == ScoreMatrix[i - 1][j - 1] + MismatchScore) {
                AlignStat[0] = Seq1[i - 1] + AlignStat[0];
                AlignStat[1] = "X" + AlignStat[1];
                AlignStat[2] = Seq2[j - 1] + AlignStat[2];
                i--;
                j--;
            } else if (ScoreMatrix[i][j] == ScoreMatrix[i - 1][j] + IndelScore) {
                AlignStat[0] = Seq1[i - 1] + AlignStat[0];
                AlignStat[1] = " " + AlignStat[1];
                AlignStat[2] = "-" + AlignStat[2];
                i--;
            } else {
                AlignStat[0] = "-" + AlignStat[0];
                AlignStat[1] = " " + AlignStat[1];
                AlignStat[2] = Seq2[j - 1] + AlignStat[2];
                j--;
            }
        }
        return AlignStat;
    }
}

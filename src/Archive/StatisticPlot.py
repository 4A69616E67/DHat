# encoding:utf-8

import numpy as np
from matplotlib.ticker import FuncFormatter
import matplotlib

matplotlib.use('Agg')
import matplotlib.pyplot as plt
import argparse


# ==============================================================================


def getargs():
    parser = argparse.ArgumentParser(description="Statistic Plot.")
    parser.add_argument('-x', '--xlabel', help="set x-label")
    parser.add_argument('-y', '--ylabel', help="set y-label")
    parser.add_argument('-i', '--input', help='input file.')
    parser.add_argument('-o', '--out', help='Out put file')
    parser.add_argument('-t', '--type', default='point',
                        help="plot type you want (default point)")
    parser.add_argument(
        '-s', '--size', help='output figure size such as 400,600')
    parser.add_argument('--title', help='output figure title')
    args = parser.parse_args()

    return args


def ScatterPlot(ax, x_value, data, legend):
    if x_value is None:
        x_value = range(data.shape[0])
    try:
        np.double(x_value)
        for i in range(data.shape[1]):
            ax.plot(np.double(x_value), data[:, i], 'o', label=legend[i])
    except(ValueError):
        for i in range(data.shape[1]):
            ax.plot(x_value, data[:, i], 'o', label=legend[i])
    if len(legend) > 1:
        ax.legend()


def BarPlot(ax, x_value, data, legend):
    if x_value is None:
        x_value = np.arange(data.shape[0])
    Width = 0.8 / data.shape[1]
    try:
        np.double(x_value)
        for i in range(data.shape[1]):
            # move x-axis
            ax.bar(np.double(x_value) + (i - 0.5 *
                                         (data.shape[1] - 1)) * Width, data[:, i], width=Width, label=legend[i])
    except(ValueError):
        for i in range(data.shape[1]):
            ax.bar(np.arange(x_value.size) + (i - 0.5 *
                                              (data.shape[1] - 1)) * Width, data[:, i], width=Width, label=legend[i])
        ax.set_xticks(np.arange(x_value.size))
        ax.set_xticklabels(list(x_value))
    if len(legend) > 1:
        ax.legend()


def StackBarPlt(ax, x_value, data, legend):
    if x_value is None:
        x_value = range(data.shape[0])
    y_offset = np.zeros(data.shape[0])
    try:
        np.double(x_value)
        for i in range(data.shape[1]):
            ax.bar(np.double(x_value), data[:, i],
                   bottom=y_offset, label=legend[i])
            y_offset = y_offset + data[:, i]
    except(ValueError):
        for i in range(data.shape[1]):
            ax.bar(range(x_value.size), data[:, i],
                   bottom=y_offset, label=legend[i])
            y_offset = y_offset + data[:, i]
        ax.set_xticks(range(x_value.size))
        ax.set_xticklabels(x_value)
    if len(legend) > 1:
        ax.legend()


def millions(x, pos):
    'The two args are the value and tick position'
    return '%.1e' % (x)


# ==============================================================================
Args = getargs()
InputFile = Args.input
X_lable = Args.xlabel
Y_lable = Args.ylabel
Title = Args.title
PlotType = Args.type
OutFile = Args.out

# InputFile="test.txt"
# PlotType="bar"
# ================read data======================
infile = open(InputFile, 'r')
Head = infile.readline().split()
if X_lable is None:
    X_lable = Head[0]
if Y_lable is None:
    Y_lable = Head[1]
X_Data = list()
Y_Data = list()
for line in infile:
    lines = line.split()
    X_Data.append(lines[0])
    Y_Data.append(lines[1:])
X_Data = np.array(X_Data)
Y_Data = np.array(Y_Data)
Y_Data = np.double(Y_Data)
infile.close()
# =============================================
Figure = plt.figure(figsize=(8, 5))
ax = Figure.add_axes([0.1, 0.1, 0.8, 0.8])
# ax.yaxis.set_major_formatter(FuncFormatter(millions))
ax.spines['right'].set_visible(False)
ax.spines['top'].set_visible(False)
ax.grid()

if PlotType == "point":
    ScatterPlot(ax, X_Data, Y_Data, Head[1:])
elif PlotType == "bar":
    BarPlot(ax, X_Data, Y_Data, Head[1:])
elif PlotType == "stackbar":
    StackBarPlt(ax, X_Data, Y_Data, Head[1:])
ax.set_xlabel(X_lable)
ax.set_ylabel(Y_lable)
if Title != None:
    ax.set_title(Title)
if OutFile is None:
    ax.imshow()
else:
    Dpi = 300
    Figure.savefig(OutFile, format=OutFile.split(".")[-1], dpi=Dpi)

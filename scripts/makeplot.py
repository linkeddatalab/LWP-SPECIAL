import os.path as path
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import argparse

from makesummary import log_summary

if __name__=="__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("-logs", type=str, nargs='+', help="log files")
	parser.add_argument("-topprune", type=int, default=0, metavar="N", help="discard N largest data point(s)")
	parser.add_argument("-bottomprune", type=int, default=0, metavar="N", help="discard N smallest data point(s)")
	parser.add_argument("-outputfile", type=str, help="when set plot to file instead of displaying")
	args = parser.parse_args()

	summaries = []
	for log in args.logs:
		if path.exists(log):
			summaries.append(log_summary(log))

	summaries = [sorted(t[2])[args.bottomprune:len(t[2])-args.topprune] for t in summaries]
	medians = [np.median(x) for x in summaries]
	print (medians)
	top = max([x[-5] for x in summaries])

	fig, ax = plt.subplots(figsize=(20, 10))
	fig.canvas.set_window_title("policy checking time")
	ax.set_title("policy checking time")
	labels = [path.splitext(path.basename(lf))[0].replace("config","") for lf in args.logs]
	#ax.set_xticklabels(labels)
	ax.set_xlabel("N widget vs Policy type")
	ax.set_ylim(0, top)
	plt.xticks(range(len(summaries)), labels)#, rotation='vertical')
	ax.yaxis.grid(True, linestyle='-', which='major', color='lightgrey',
               alpha=0.5) 
	pos = np.arange(len(summaries)) + 1
	upperLabels = [str(np.round(s, 2)) for s in medians]
	weights = ['bold', 'semibold']
	for tick, label in zip(range(len(summaries)), ax.get_xticklabels()):
		k = tick % 2
		ax.text(pos[tick], top - (top*0.05), upperLabels[tick],
		         horizontalalignment='center', size='x-small', weight=weights[k])
	extramsg = "all" if args.topprune+args.bottomprune == 0 else ""
	if args.topprune > 0:
		extramsg += "w/o top {} ".format(args.topprune)
	if  args.bottomprune > 0:
		extramsg += "w/o bottom {} ".format(args.bottomprune)

	ax.set_ylabel("time (ms) - {}".format(extramsg))
	ax.boxplot(summaries, sym='+', labels=labels)
	if args.outputfile is not None:
		plt.savefig(args.outputfile)
	else:
		plt.show()

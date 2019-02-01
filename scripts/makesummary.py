import numpy as np
import os
import os.path as path
import argparse

def log_summary(filename):
	num_true = 0
	num_false = 0
	times = []
	with open(filename, "r") as flog:
		for line in flog:
			if line.startswith("checked"):
				tmp = line.strip()
				tmp = tmp.split(" in ")[1].split(" ms : ")
				if tmp[1] == "true":
					num_true += 1
				else:
					num_false += 1
				times.append(float(tmp[0]))

	return num_true, num_false, times

if __name__=="__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("-inputFile", help="output log from mashup-privacy-checker program", required=True)
	args = parser.parse_args()

	if not path.exists(args.inputFile):
		exit(1)
	else:
		num_true, num_false, times = log_summary(args.inputFile)
		
		print("*"*80)
		print("log filename : {}".format(args.inputFile))
		print("*"*80)
		print("number of checking : {} ".format(len(times)))
		print("checking result (true/false) : {}/{}".format(num_true, num_false))
		times = np.vstack(times)
		print("min checking time (ms) : {}".format(np.min(times)))
		print("max checking time (ms) : {}".format(np.max(times)))
		print("median checking time (ms) : {}".format(np.median(times)))
		print("Q1 checking time (ms) : {}".format(np.quantile(times, 0.25)))
		print("Q3 checking time (ms) : {}".format(np.quantile(times, 0.75)))
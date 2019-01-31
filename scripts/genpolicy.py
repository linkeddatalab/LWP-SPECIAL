from rdflib import Namespace, Graph, URIRef, BNode, Literal, RDF, RDFS, OWL
import os
import sys
import copy
import random
import argparse

from vocab import *

def create_list(g, l):
	if l is None:
		return RDF.nil
	else:
		root = BNode()
		node = root
		for i, res in enumerate(l):
			g.add((node, RDF.first, res))
			next_node = RDF.nil if i==len(l)-1 else BNode()
			g.add((node, RDF.rest, next_node))
			node = next_node
		return root

def add_objects(g, s, p, objectlist):
	for item in objectlist:
		g.add((s, p, item))

def create_policy(g, pname, aspects, make_intersection=False):
	policy_uri = LWR[pname]
	g.add((policy_uri, RDF.type, OWL.Class))

	nodes = []
	for prop, pvalues in aspects:
		p_node = BNode()
		nodes.append(p_node)
		g.add((p_node, RDF.type, OWL.Restriction))
		g.add((p_node, OWL.onProperty, prop))
		if make_intersection and len(dp)>1:
			g.add((dp_node, OWL.intersectionOf, create_list(g, pvalues)))
		else:
			add_objects(g, p_node, OWL.someValuesFrom, pvalues)

	eqCls = BNode()
	g.add((eqCls, OWL.intersectionOf, create_list(g, nodes)))
	g.add((policy_uri, OWL.equivalentClass, eqCls))

	return policy_uri

def random_subset(l, n=0):
	if len(l)==1:
		return l
	else:
		num_subset = random.randint(1, len(l)) if n<1 else n
		result = copy.copy(l)
		while len(result)>num_subset:
			item_to_remove = random.choice(result)
			result.remove(item_to_remove)
		return result

if __name__=="__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("-refVocab", type=str, metavar="vocab.ttl", help="reference vocabulary used to generate the policy (unused atm.)")
	parser.add_argument("-dirOutput", type=str, metavar="DIR", default=os.getcwd(), help="output directory to write the output file(s)")
	parser.add_argument("-numPolicies", type=int, metavar="N", default=1, help="create N policies in the output directory")
	parser.add_argument("-outputFile", type=str, metavar="FILE", help=".ttl file to save the generated policy")
	parser.add_argument("-intersection", action="store_true", default=False, help="in case of multiple values generated, it will be written as intersection instead of list of objects")
	args = parser.parse_args()

	#print(args)

	for n_pol in range(args.numPolicies):
		g = Graph()
		g.bind("owl", OWL)
		bindNS(g)
		mashup_name = "{}{}Mashup".format("Random", random.randint(0,65536))
		policy_name = "{}_Policy".format(mashup_name)

		#
		# TODO: load reference vocabulary file, collect names and aspects (properties)
		#

		dataPolicy = random_subset([ SVD["UserLocation"] ])
		processPolicy = random_subset([ SVPR["AnyProcessing"],SVPR["Anonymize"],SVPR["Aggregate"] ], 2)
		purposePolicy = random_subset([ SVPU["Develop"] ])
		recipientPolicy = random_subset([ SVPR["Public"] ])
		storagePolicy = random_subset([ SVL["AT"] ])

		items = []
		items.append((LW["hasData"], dataPolicy))
		items.append((LW["hasPurpose"], purposePolicy))
		items.append((LW["hasProcessing"], processPolicy))
		items.append((LW["hasRecipient"], recipientPolicy))
		items.append((LW["hasStorage"], storagePolicy))

		create_policy(g, policy_name, items, args.intersection)

		outfn = args.outputFile if n_pol==1 and args.outputFile is not None else "policy{}.ttl".format(n_pol+1)
		print(outfn)
		outdir = os.path.join(os.getcwd(), args.dirOutput)
		if not os.path.exists(outdir):
			os.makedirs(outdir)
		outfull = os.path.join(outdir, outfn)
		with open(outfull, "wb") as fttl:
			fttl.write(g.serialize(format="turtle"))	
		#print(g.serialize(format="turtle").decode("latin-1"))
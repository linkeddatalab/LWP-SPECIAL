from rdflib import Namespace, Graph, URIRef, BNode, Literal, RDF, RDFS, OWL
import os
import os.path as path
import sys
import copy
import random
import argparse

from vocab import *
from inference import *

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

def make_restriction(g, prop):
	p_node = BNode()
	
	g.add((p_node, RDF.type, OWL.Restriction))
	g.add((p_node, OWL.onProperty, prop))
	return p_node

def create_policy(g, pname, aspects):
	policy_uri = LWR[pname]
	g.add((policy_uri, RDF.type, OWL.Class))

	nodes = []
	for prop, pvalues, rel in aspects:
		p_node = make_restriction(g, prop)
		nodes.append(p_node)
		
		hasSub = isinstance(pvalues, tuple)
		if hasSub:
			#print(pvalues)
			npvalues = []
			for sprop, spvals, srel in pvalues:
				subr = make_restriction(g, sprop)
				npvalues.append(subr)
				if srel in [OWL.intersectionOf, OWL.unionOf]:# and len(pvalues)>1:
					g.add((subr, srel, create_list(g, spvals)))
				else:
					add_objects(g, subr, srel, spvals)
			pvalues = npvalues
		if hasSub or rel in [OWL.intersectionOf, OWL.unionOf]:# and len(pvalues)>1:
			g.add((p_node, rel, create_list(g, pvalues)))
		else:
			add_objects(g, p_node, rel, pvalues)

	eqCls = BNode()
	g.add((eqCls, OWL.intersectionOf, create_list(g, nodes)))
	g.add((policy_uri, OWL.equivalentClass, eqCls))

	return policy_uri

def random_subset(l, n=0):
	if len(l)==1:
		return l
	else:
		if n==1:
			return [random.choice(l)]
		else:
			num_subset = random.randint(1, len(l)) if n<1 else n
			result = copy.copy(l)
			while len(result)>num_subset:
				item_to_remove = random.choice(result)
				result.remove(item_to_remove)
			return result

def getRandomValues(g, rootClass, allowRoot=False, reverse=True, num_subset=1, withFilter=False):
	_g = Graph()
	_g += g
	triples, sc = inferProp(g, rootClass, [], RDFS.subClassOf)
	for t in triples:
		_g.add(t)
	#print(g.qname(rootClass), len(sc))
	pick = random_subset(sc + [rootClass] if allowRoot else sc, num_subset)
	#print(len(pick), num_subset)
	result = []
	#withFilter = False
	if not withFilter:
		result = pick
	else:
		for w in pick:
			# 1st case
			if len(result)==0:
				result.append(w)
				continue
			if reverse:
				# narrower over broader
				_toRemove = []
				for m in result:
					addToResult = True
					if (m, RDFS.subClassOf, w) in _g:
						# no need to insert if narrower concept already covers w
						addToResult = False
						break
					if (w, RDFS.subClassOf, m) in _g:
						# flag broader concepts to be removed later
						_toRemove.append(m)
				for r in _toRemove:
					result.remove(r)
				if addToResult:
					result.append(w)
			else:
				# broader over narrower
				_toRemove = []
				for m in result:
					addToResult = True
					if (w, RDFS.subClassOf, m) in _g:
						# no need to insert as broader concept already covers w
						addToResult = False
						break
					if (m, RDFS.subClassOf, w) in _g:
						# flag narrower concepts to be removed later
						_toRemove.append(m)
				for r in _toRemove:
					result.remove(r)
				if addToResult:
					result.append(w)
	return result

if __name__=="__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("-refVocab", type=str, metavar="vocab.ttl", help="reference vocabulary used to generate the policy (unused atm.)", required=True)
	parser.add_argument("-dirOutput", type=str, metavar="DIR", default=os.getcwd(), help="output directory to write the output file(s)")
	parser.add_argument("-numPolicies", type=int, metavar="N", default=1, help="create N policies in the output directory")
	parser.add_argument("-outputFile", type=str, metavar="FILE", help=".ttl file to save the generated policy")
	#parser.add_argument("-intersection", action="store_true", default=False, help="in case of multiple values generated, it will be written as intersection instead of list of objects")
	parser.add_argument("-allowTop", action="store_true", default=False, help="when set, allows top concepts (spl:AnyX) to be included")
	parser.add_argument("-genCount", type=int, default=1, help="set fixed set of values to be generated")
	args = parser.parse_args()

	#print(args)
	try:
		rg = Graph()
		rg.parse(args.refVocab.replace(os.sep, "/"), format="turtle")
	except:
		print("reference vocabulary cannot be accessed")
		exit(1)

	for n_pol in range(args.numPolicies):
		g = Graph()
		g.bind("owl", OWL)
		bindNS(g)
		mashup_name = "{}{}Mashup".format("Random", random.randint(0,65536))
		policy_name = "{}_Policy".format(mashup_name)

		#
		# TODO: load reference vocabulary file, collect names and aspects (properties)
		#
		NarrowerPriority = False
		removeRedundant = True
		dataPolicy = getRandomValues(rg, SPL["AnyData"], args.allowTop, NarrowerPriority, args.genCount + random.randint(0,5), removeRedundant)
		processPolicy = getRandomValues(rg, SPL["AnyProcessing"], args.allowTop, NarrowerPriority, args.genCount, removeRedundant)
		purposePolicy = getRandomValues(rg, SPL["AnyPurpose"], args.allowTop, NarrowerPriority, args.genCount, removeRedundant)	
		recipientPolicy = getRandomValues(rg, SPL["AnyRecipient"], args.allowTop, NarrowerPriority, args.genCount, removeRedundant)
		locationPolicy = getRandomValues(rg, SPL["AnyLocation"], args.allowTop, NarrowerPriority, args.genCount, removeRedundant)
		durationPolicy = getRandomValues(rg, SPL["AnyDuration"], args.allowTop, NarrowerPriority, args.genCount, removeRedundant)
		# subAspect
		storagePolicy = ((SPL["hasLocation"], locationPolicy, OWL.someValuesFrom), (SPL["hasDuration"], durationPolicy, OWL.someValuesFrom))

		items = []
		items.append((SPL["hasData"], dataPolicy, OWL.unionOf))
		items.append((SPL["hasProcessing"], processPolicy, OWL.intersectionOf))
		items.append((SPL["hasStorage"], storagePolicy, OWL.someValuesFrom))

		# mashup specific	
		items.append((SPL["hasPurpose"], purposePolicy, OWL.unionOf))
		items.append((SPL["hasRecipient"], recipientPolicy, OWL.someValuesFrom))
		
		create_policy(g, policy_name, items)#, args.intersection)

		outfn = args.outputFile if n_pol==1 and args.outputFile is not None else "policy{}.ttl".format(n_pol+1)
		print(outfn)
		outdir = os.path.join(os.getcwd(), args.dirOutput)
		if not os.path.exists(outdir):
			os.makedirs(outdir)
		outfull = os.path.join(outdir, outfn)
		with open(outfull, "wb") as fttl:
			fttl.write(g.serialize(format="turtle"))	
		#print(g.serialize(format="turtle").decode("latin-1"))
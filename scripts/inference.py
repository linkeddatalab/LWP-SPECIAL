"""
contain simple inference (deriving transitiveProperty)
"""
from rdflib import Graph, RDFS, OWL

def getSubclasses(g, c):
	result = []
	for s,_,_ in g.triples((None, RDFS.subClassOf, c)):
		result += [s] + getSubclasses(g, s)
	return result

def getTopConcepts(g, prop=RDFS.subClassOf, root=OWL.Class):
	topclasses = []
	q = """
	SELECT DISTINCT ?c 
	WHERE {{ 
		?c a <{0}> . ?d <{1}> ?c 
		FILTER NOT EXISTS {{ ?c <{1}> ?e }} 
	}}
	""".format(root, prop)
	#print(q)
	for t in g.query(q):
		topclasses.append(t[0])
	return topclasses

def inferProp(g, c, supl, prop=RDFS.subClassOf):
	inftriples = []
	subclasses = []
	for s,_,_ in g.triples((None, prop, c)):
		subclasses.append(s)
		for suc in supl:
			tmp = (s, prop, suc)
			if tmp not in g:
				inftriples.append(tmp)
		_t, _s = inferProp(g, s, [c]+supl, prop)
		inftriples += _t
		subclasses += _s
	return inftriples, subclasses

def inferTransitive(g, prop=RDFS.subClassOf, cls=OWL.Class, apply=False):
	triples = []
	subs = []
	for t in getTopConcepts(g, prop, cls):
		_tt, _ss = inferProp(g, t, [], prop)
		triples += _tt
		subs += _ss

	if apply:
		for t in triples:
			g.add(t)
	return triples

if __name__=="__main__":
	g = Graph()
	g.parse("../data/cpss-special-integrated.ttl", format="turtle")

	"""
	topclasses = getTopConcepts(g, RDFS.subClassOf, OWL.Class)

	for t in topclasses:
		print('-'*80)
		print(t)
		for s,_,_ in g.triples((None, RDFS.subClassOf, t)):
			sc = getSubclasses(g, s)
			print(' ', s)
			if len(sc)>0:
				for c in sc:
					print('  ', c)
	"""

	InfResult = inferTransitive(g, RDFS.subClassOf, OWL.Class, False)
	print(len(InfResult))
	for t in InfResult:
		g.add(t)
		print(g.qname(t[0]), ' < ', g.qname(t[2]))#, ' : ', t in g)
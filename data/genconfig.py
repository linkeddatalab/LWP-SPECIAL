from rdflib import Graph, RDF, OWL, BNode, URIRef, Namespace
import glob
import os
import os.path as path
import argparse

def makeList(g, l):
	lnode = BNode()
	node = lnode
	for i, item in enumerate(l):
		g.add((node, RDF.first, URIRef(item)))
		next_node = BNode() if i<len(l)-1 else RDF.nil
		g.add((node, RDF.rest, next_node))
		node = next_node
	return lnode

if __name__=="__main__":
	parser = argparse.ArgumentParser()
	parser.add_argument("-policyDir", default="simplePolicies", help="directory containing privacy policy in turtle format")
	parser.add_argument("-refPolicy", default=["cpss-special-integrated.ttl"], nargs='+', help="filename containing reference vocabulary")
	parser.add_argument("-mashupPolicy", default="userlocationpolicy.ttl", help="filename containing mashup policy")
	parser.add_argument("-outputCfg", required=True, help="filename of output configuration file (.ttl)")
	parser.add_argument("-limit", default=-1, metavar="N", help="limit number of checking configuration to N checking(s)")
	args = parser.parse_args()

	print(args)

	# output configuration file
	g_config = Graph()
	CPSS = Namespace("http://w3id.org/cityspin/ns/special-cpss#")
	g_config.bind("cpss", CPSS[""])
	root = BNode()

	# mashup policy file (generate policy using java -jar privacycheck -generate mashup-config.ttl mashup-policy.ttl)
	g_mashup = Graph()
	g_mashup.parse(args.mashupPolicy, format="turtle")
	
	subjects = [x[0] for x in list(g_mashup.triples((None, RDF.type, OWL.Class))) if isinstance(x[0], URIRef)]
	print(subjects)
	mashup_policy_res = subjects[0]
	print(mashup_policy_res)

	# user policy
	files = glob.glob(path.join(args.policyDir, "*.ttl"))
	n = 0
	for userpolicy in files:
		print(userpolicy)
		g_policy = Graph()
		g_policy.parse(files[0], format="turtle")

		user_policy_res = list(g_policy.triples((None, RDF.type, OWL.Class)))[0][0]
		print(user_policy_res)

		cfg = BNode()
		g_config.add((cfg, CPSS["refPolicy"], makeList(g_config, args.refPolicy)))
		g_config.add((cfg, CPSS["userPolicy"], URIRef(userpolicy.replace(path.sep, "/"))))
		g_config.add((cfg, CPSS["usagePolicy"], URIRef(args.mashupPolicy)))
		g_config.add((cfg, CPSS["usageSubj"], mashup_policy_res))
		g_config.add((cfg, CPSS["userSubj"], user_policy_res))
		g_config.add((root, CPSS["hasConfig"], cfg))

		n += 1
		#if n > 5: break

	with open(args.outputCfg, "wb") as fo:
		fo.write(g_config.serialize(format="turtle"))
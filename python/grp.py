# Python script for computing the control matrix G in Gallego's Recovery Policy
# This script assumes that the slycot package has been installed (as well as Scipy)
# Fernando Tubilla Nov 2016

import sys
import slycot
import json
import string as SR
from pylab import *

# Path to the JSON input file
json_params = sys.argv[1]
data = json.loads(json_params)
# Gallego's paper normalizes the data by dividing over the demand rates, which we do here
d_raw = array(data['demandRates'])
d = ones(d_raw.size)
# Note that the production rate is compensated for the machine efficiency
p = array(data['productionRates']) * float(data["machineEfficiency"]) / d_raw
h = array(data["inventoryHoldingCosts"]) * d_raw
b = array(data["backlogCosts"]) * d_raw

# Production sequence separated (e.g. [0,1,2,0,1])
seq = SR.replace(sys.argv[2], "[","")
seq = SR.replace(seq,"]","")
f = map(int, seq.split(','))
if min(f) > 0 :
	raise ValueError("The item indices must start at 0")

# Length of the sequence
n = len(f)
m = len(d)

#F Matrix
F =  zeros((n,m)) 
for j in range(0,n):
	for i in range(0,m):
		if f[j] == i: 
			F[j,i] = 1

#Q Matrix
Q = diag(p) - ones((m,m)) 

#R and S Matrices
FQF = dot(F, dot( Q, F.T ) )
R = zeros((n,n))
S = zeros((n,n))
for i in range(0,n):
	for j in range(0,n):
		if j <= i: R[i,j] = FQF[i,j]
		if j <  i: S[i,j] = FQF[i,j]

#B and H
B = diag( p*b / ( p - 1 ) )
H = diag( p*h / ( p - 1 ) )
Bb = zeros((n,n))
Hb = zeros((n,n))
FBF = dot( F, dot( B, F.T ) )
FHF = dot( F, dot( H, F.T ) )
for i in range(0,n):
	for j in range(0,n):
		if i==j:
			Bb[i,j] = FBF[i,j]
			Hb[i,j] = FHF[i,j]

#C,D, and E
C =  dot( F.T, dot( Bb + Hb, F ) )
D = -dot( F.T, ( dot( Bb, S ) + dot( Hb, R ) ) )
E =  dot( S.T, dot( Bb, S ) ) + dot( R.T, dot( Hb, R ) )

#Prepare the inputs for calling Slicot
C = matrix( C )
Q = matrix( Q )
F = matrix( F )
D = matrix( D )
E = matrix( E )

QFT = array( Q*(F.T) )
	
#Solve the ARME
#Note: The definitions of n and m are reversed in this function
# Slycot solves: X = A'XA - (L + A'XB)(R + B'XB)^-1 (L+A'XB)' + Q
# While GRP solves M = M + C - ( MQF' - D )(E + FQ'MQF )^-1(MQF' - D)'
# Arguments (n, m, A, B, Q, R, dico, [p, L, fact, uplo, sort, tol, ldwork])
M, rcond, w, S, T = slycot.sb02od( m, n, eye(m), QFT, C, E, dico="D", L= -D )

#	Compute the error to ensure that slycot solved the problem correctly
X = inv( E + F * ( Q.T ) * M * Q * ( F.T ) )
Y = M * Q * ( F.T ) - D
Mnew = M + C - Y * X * ( Y.T )

maxerror = 0.0
for i in range(0, m):
	for j in range(0, m):
		error = abs( ( M[i,j] - Mnew[i,j] ) / M[i,j] )
		if error > maxerror:
			maxerror = error
print "Max error in ARME is %.8f" % maxerror

if maxerror > data["tolerance"]:
	sys.stderr.write("TOLERANCE NOT MET: ARME tolerance not met! Max rel error %s and tolerance %s\n" % (maxerror, data["tolerance"]) )

M = array(M)

# Compute the Gain Matrix. Note that we need to scale back by the demand rates because G is unitless but we want
# v = G * z, where v has units of time and z has units of product.
G = array( ( ( E + F * ( Q.T ) * M * Q * ( F.T ) ).I ) * ( ( M * Q * ( F.T ) - D ).T) )
for i in range(0, n):
	for j in range(0, m):
		print i, j, G[i,j] / d_raw[j]


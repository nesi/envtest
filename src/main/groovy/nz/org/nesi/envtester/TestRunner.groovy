package nz.org.nesi.envtester

def config = ['host':'pan.nesi.org.nz']

EnvTest test = new IPerfTest(config)
//EnvTest test = new TraceRouteTest(config)

test.start()

EnvTestResult result = test.getResult()

println 'Test: '+result.getTestName()
println '\t'+result.getTestDescription()

println 'Result:'
print '\n'+result.getResult()
if ( result.isFailed() ) {
	println ' (failed)'
} else {
	println ' (success)'
}

println '\nDetails:'
println '\n'+result.getDetails().join('\n')

if ( result.isFailed() ) {
	println '\nError(s):'
	println '\n'+result.getErrors().join('\n')
}
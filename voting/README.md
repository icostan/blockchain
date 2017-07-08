# Voting

## Manual deploy new Ethereum contract

### Init client
> Web3 = require('web3')
> web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));

### Compile
> code = fs.readFileSync('Voting.sol').toString()
> solc = require('solc')
> compiledCode = solc.compile(code)

### Deploy
> abiDefinition = JSON.parse(compiledCode.contracts[':Voting'].interface)
> VotingContract = web3.eth.contract(abiDefinition)
> byteCode = compiledCode.contracts[':Voting'].bytecode
> deployedContract = VotingContract.new(['Rama','Nick','Jose'],{data: byteCode, from: web3.eth.accounts[0], gas: 4700000})
> deployedContract.address
> contractInstance = VotingContract.at(deployedContract.address)

### Usage
> contractInstance.totalVotesFor.call('Rama')
> contractInstance.voteForCandidate('Rama', {from: web3.eth.accounts[0]})
> contractInstance.voteForCandidate('Rama', {from: web3.eth.accounts[0]})
> contractInstance.totalVotesFor.call('Rama').toLocaleString()

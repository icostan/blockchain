pragma solidity ^0.4.2;

import "truffle/Assert.sol";
import "truffle/DeployedAddresses.sol";
import "../contracts/Voting.sol";

contract TestVoting {
  function testTotalVotesFor() {
    Voting voting = Voting(DeployedAddresses.Voting());

    uint expected = 0;
    Assert.equal(voting.totalVotesFor("Rama"), expected, "He should have 0 initial votes");
  }
  
  function testVoteForCandidate() {
    Voting voting = Voting(DeployedAddresses.Voting());

    voting.voteForCandidate("Rama");
    uint expected = 1;
    Assert.equal(voting.totalVotesFor("Rama"), expected, "He should have 1 votes");
  }

  function testWinnerName() {
    Voting voting = Voting(DeployedAddresses.Voting());

    voting.voteForCandidate("Rama");
    voting.voteForCandidate("Nick");
    voting.voteForCandidate("Nick");
    
    bytes32 expected = 'Nick';
    Assert.equal(voting.winnerName(), expected, "Nick should be the winner");
  }
}

package jforgame.demo.game.crossrank;

/**
 *  cross server rank based on Redis SortedSet
 *  redis sortedset's score is  double type, which has only 52 bits
 *
 */
public interface CrossRank {
	
	int getRankType();

	/**
	 * local server id
	 * @return
	 */
	int getServerId();

	long getCreateTime() ;
	
	long getPlayerId();

	/** 
	 * first level rank score
	 * @return
	 */
	int getScore() ;

	/** 
	 * second level rank score
	 * @return
	 */
	int getAid() ;

	/** redis rank type key */
	String buildRankKey();
	
	/** redis rank record key */
	String buildResultKey();

	/** redis rank score */
	double buildRankScore();

}

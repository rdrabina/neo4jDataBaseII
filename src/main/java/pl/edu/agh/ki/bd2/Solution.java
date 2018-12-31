package pl.edu.agh.ki.bd2;


public class Solution {

    private final GraphDatabase graphDatabase = GraphDatabase.createDatabase();

    public void databaseStatistics() {
        System.out.println(graphDatabase.runCypher("CALL db.labels()"));
        System.out.println(graphDatabase.runCypher("CALL db.relationshipTypes()"));
    }

    public void runAllTests() {
        String actorName = "Batman";
        String movieName = "New York in danger";
        String birthPlace = "Warsaw";
        String biography = "Batman was an American motion-picture and television producer and showman, famous as " +
                "a pioneer of cartoon films and as the creator of Disneyland.";
        String userLogin = "Sangreal";
        String actorOne = "Robert De Niro";
        String actorTwo = "Armin Mueller-Stahl";
        String filmName = "Star Wars";
        System.out.println(findActorByName(actorOne));
        System.out.println(findMovieByTitleLike(filmName));
        System.out.println(findRatedMoviesForUser(userLogin));
        System.out.println(findCommonMoviesForActors(actorOne, actorTwo));
        System.out.println(findMovieRecommendationForUser());
        System.out.println(createActorNode(actorName));
        System.out.println(createMovieNode(movieName));
        System.out.println(createRelationshipBetweenTwoNodes(actorName, movieName));
        System.out.println(setActorProperties(actorName, birthPlace, biography));
        System.out.println(findActorsWhoActedInAtLeastSixMovies());
        System.out.println(getAverageActedInForActorsWhoActedInAtLeastSevenMovies());
        System.out.println(findActorsWhoActedInAtLeastFiveMoviesAndDirectedAtLeastOne());
        System.out.println(findUserFriendWhoRatedMovieAtLeastThreeStars(userLogin));
        System.out.println(findPathBetweenTwoActors(actorOne, actorTwo));
        System.out.println("With index: " + findQueryExecutionTimeWithoutIndex(actorOne) + "ms.\tWith index: " + findQueryExecutionTimeWithIndex(actorOne) + "ms.");
        System.out.println("Without index: " + findPathQueryExecutionTimeWithoutIndex(actorOne, actorTwo) + "ms.\tWith index: " + findPathQueryExecutionTimeWithIndex(actorOne, actorTwo) + "ms.");
    }

    private String findActorByName(final String actorName) {
        return graphDatabase.runCypher("MATCH (actor:Actor{name: \"" + actorName +"\"}) RETURN actor");
    }

    private String findMovieByTitleLike(final String movieName) {
        return graphDatabase.runCypher("MATCH (movie:Movie) WHERE movie.title contains \"" + movieName + "\" RETURN movie");
    }

    private String findRatedMoviesForUser(final String userLogin) {
        return graphDatabase.runCypher("MATCH (:User{name: \"" + userLogin + "\"})-[:RATED]->(movie:Movie) RETURN movie");
    }

    private String findCommonMoviesForActors(String actorOne, String actorTwo) {
        return graphDatabase.runCypher("MATCH (actor:Actor)-[:ACTS_IN]->(movie:Movie) WHERE actor.name = \"" + actorOne + "\" or " +
                "actor.name = \"" + actorTwo + "\" RETURN actor, movie");
    }

    private String findMovieRecommendationForUser() {
        return graphDatabase.runCypher("MATCH (:User)-[r:RATED]->(movie:Movie) WHERE r.stars > 4 RETURN DISTINCT movie.title");
    }

    private String createActorNode(String actorName) {
        return graphDatabase.runCypher("CREATE (actor:Actor {name: \"" + actorName + "\"}) RETURN actor");
    }

    private String createMovieNode(String movieName) {
        return graphDatabase.runCypher("CREATE (movie:Movie {title: \"" + movieName + "\"}) RETURN movie");
    }

    private String createRelationshipBetweenTwoNodes(String actorName, String movieName) {
        return graphDatabase.runCypher("MATCH (a:Actor {name: \"" + actorName + "\"}), (m:Movie {title: \"" + movieName + "\"})" +
                "CREATE (a)-[:ACTS_IN]->(m) RETURN a, m");
    }

    private String setActorProperties(String actorName, String birthPlace, String biography) {
        return graphDatabase.runCypher("MATCH (actor:Actor {name: \"" + actorName + "\"}) SET actor.birthplace = \"" +
                birthPlace + "\", actor.biography = \"" + biography + "\" RETURN actor");
    }

    private String findActorsWhoActedInAtLeastSixMovies() {
        return graphDatabase.runCypher("MATCH (actor:Actor)-[:ACTS_IN]->(movie:Movie) WITH actor, COLLECT (DISTINCT movie.title) as movies " +
                "WHERE LENGTH(movies) > 5 RETURN actor.name, LENGTH(movies) ORDER BY actor.name");
    }

    private String getAverageActedInForActorsWhoActedInAtLeastSevenMovies() {
        return graphDatabase.runCypher("MATCH (actor:Actor)-[:ACTS_IN]->(movie:Movie) WITH actor, COLLECT (DISTINCT movie.title) as movies, " +
                "COUNT(DISTINCT actor) as actors WHERE LENGTH(movies) > 6 RETURN SUM(LENGTH(movies)) * 1.0 / SUM(actors)");
    }

    private String findActorsWhoActedInAtLeastFiveMoviesAndDirectedAtLeastOne() {
        return graphDatabase.runCypher("MATCH (movie:Movie)<-[:ACTS_IN]-(person:Person)-[:DIRECTED]->(movie1:Movie) WITH person, " +
                "COLLECT(DISTINCT movie.title) as movies, COLLECT(DISTINCT movie1.title) as directedMovies " +
                "WHERE LENGTH(movies) > 4 AND LENGTH(directedMovies) > 0 RETURN person.name, LENGTH(movies), LENGTH(directedMovies) " +
                "ORDER BY LENGTH(movies)");
    }

    private String findUserFriendWhoRatedMovieAtLeastThreeStars(String userLogin) {
        return graphDatabase.runCypher("MATCH (user)-[:FRIEND]->(friend)-[rated:RATED]->(movie:Movie) WHERE user.name = \"" + userLogin + "\" " +
                "AND rated.stars > 2 RETURN friend, movie.title, rated.stars");
    }

    private String findPathBetweenTwoActors(String actorOne, String actorTwo) {
        return graphDatabase.runCypher("MATCH p = shortestPath((one:Person {name: \"" + actorOne + "\"})" +
                "-[*]-(two:Person {name: \"" + actorTwo + "\"})) RETURN FILTER(x in NODES(p) WHERE EXISTS(x.biography))");
    }

    private long findQueryExecutionTimeWithoutIndex(String actor) {
        return executeQueryWithTime("PROFILE MATCH (actor:Actor {name: \"" + actor + "\"}) RETURN actor.name");
    }

    private long findQueryExecutionTimeWithIndex(String actor) {
        graphDatabase.runCypher("CREATE INDEX ON :Actor(name)");
        long time = findQueryExecutionTimeWithoutIndex(actor);
        graphDatabase.runCypher("DROP INDEX ON :Actor(name)");
        return time;
    }

    private long findPathQueryExecutionTimeWithoutIndex(String actorOne, String actorTwo) {
        return executeQueryWithTime("PROFILE MATCH p = shortestPath((one:Person {name: \"" + actorOne + "\"})" +
                "-[*]-(two:Person {name: \"" + actorTwo + "\"})) RETURN p");
    }

    private long findPathQueryExecutionTimeWithIndex(String actorOne, String actorTwo) {
        graphDatabase.runCypher("CREATE INDEX ON :Actor(name)");
        long time = findPathQueryExecutionTimeWithoutIndex(actorOne, actorTwo);
        graphDatabase.runCypher("DROP INDEX ON :Actor(name)");
        return time;
    }

    private long executeQueryWithTime(String query) {
        long startTime = System.currentTimeMillis();
        graphDatabase.runCypher(query);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

}

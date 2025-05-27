import java.sql.*;
import java.util.*;
import java.util.Timer;

public class Quiz {
    private static final int QUIZ_TIME_LIMIT = 10 * 60; // 10 minutes in seconds
    private static Scanner scanner = new Scanner(System.in);
    private static int score = 0;
    private static int timeTaken = 0;

    public static void main(String[] args) {
        List<Question> questions = fetchQuestions();
        if (questions.isEmpty()) {
            System.out.println("No questions found in the database.");
            return;
        }

        startTimer();

        for (Question question : questions) {
            askQuestion(question);
        }

        saveUserScore("user", score, timeTaken);
        displayRankings();
    }

    // Fetch questions from the database
    private static List<Question> fetchQuestions() {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM questions";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"),
                        rs.getString("question"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        rs.getString("correct_option")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    // Start a timer for the quiz
    private static void startTimer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeTaken++;
                if (timeTaken >= QUIZ_TIME_LIMIT) {
                    System.out.println("Time's up!");
                    System.exit(0); // End the quiz when time is up
                }
            }
        }, 0, 1000); // 1000 ms = 1 second
    }

    // Ask a question and evaluate the user's answer
    private static void askQuestion(Question question) {
        System.out.println(question.getQuestion());
        System.out.println("A. " + question.getOptionA());
        System.out.println("B. " + question.getOptionB());
        System.out.println("C. " + question.getOptionC());
        System.out.println("D. " + question.getOptionD());

        String answer = scanner.nextLine().toUpperCase();
        if (answer.equals(question.getCorrectOption())) {
            score++;
        }
    }

    // Save user score to the database
    private static void saveUserScore(String username, int score, int timeTaken) {
        String query = "INSERT INTO user_scores (username, score, time_taken) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setInt(2, score);
            stmt.setInt(3, timeTaken);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Display rankings (top scores)
    private static void displayRankings() {
        String query = "SELECT * FROM user_scores ORDER BY score DESC LIMIT 10";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            System.out.println("\nTop Rankings:");
            while (rs.next()) {
                String username = rs.getString("username");
                int score = rs.getInt("score");
                int timeTaken = rs.getInt("time_taken");

                System.out.println(username + " - Score: " + score + ", Time: " + timeTaken + "s");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
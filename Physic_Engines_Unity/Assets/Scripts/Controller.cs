using System.Collections.Generic;
using System.IO;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.SceneManagement;

/**
 * Done by KPP (pern) in 2025!
 */

public class Controller : MonoBehaviour
{
    // references to the board and the dart rigid bodies
    private Rigidbody dart;
    private Rigidbody board;

    // the time series with the data to export
    private readonly List<TimeSeriesData> timeSeries = new();

    // flag that indicates if the dart has been launched
    private bool isLaunched = false;

    // stores the current time for exporting the time series
    private float currentTime = 0;

    // the MoveDart-script (to stop recording the time series after the collision)
    private MoveDart moveDart;

    // Input actions
    private InputAction reloadAction;
    private InputAction launchAction;
    private InputAction saveAction;

    // Start is called before the first frame update
    void Start()
    {
        // store references to the dart and board rigid bodies
        // Note: The Board tag was added to the dart board game object in Unity
        dart = GameObject.Find("SM_Prop_Dart_01").GetComponent<Rigidbody>();
        board = GameObject.FindWithTag("Board").GetComponent<Rigidbody>();
        Debug.Assert(dart != null);
        Debug.Assert(board != null);

        // Get the MoveDart component (to stop recording the time series after the collision)
        moveDart = dart.GetComponent<MoveDart>();
        Debug.Assert(moveDart != null);

        // Initialize input actions
        var playerInput = new InputActionMap("Player");

        reloadAction = playerInput.AddAction("Reload", binding: "<Keyboard>/r");
        launchAction = playerInput.AddAction("Launch", binding: "<Keyboard>/space");
        saveAction = playerInput.AddAction("Save", binding: "<Keyboard>/o");

        reloadAction.performed += ctx => ReloadScene();
        launchAction.performed += ctx => LaunchDart();
        saveAction.performed += ctx => WriteTimeSeriesToCSV();

        playerInput.Enable();
    }

    // Update is called once per frame
    void Update()
    {
        // Input actions are handled by the Input System callbacks
    }
    // FixedUpdate is called once per frame at a defined interval (typically 20 ms)
    void FixedUpdate()
    {
        // check if the dart has been launched
        // Note: OnCollision is called after FixedUpdate, so this code should also capture the last data point correctly
        if (isLaunched && !moveDart.hitBoard)
        {
            float DartDeltaVY = 0.0f;
            float DartDeltaVZ = 0.0f;
            float BoardDeltaVY = 0.0f;
            float BoardDeltaVZ = 0.0f;
            
            if (timeSeries.Count > 0) {
                TimeSeriesData lastEntry = timeSeries[timeSeries.Count - 1];
                DartDeltaVY = (dart.linearVelocity.y - lastEntry.DartVelocityY) / Time.fixedDeltaTime;
                DartDeltaVZ = (dart.linearVelocity.z - lastEntry.DartVelocityZ) / Time.fixedDeltaTime;
                BoardDeltaVY = (board.linearVelocity.y - lastEntry.BoardVelocityY) / Time.fixedDeltaTime;
                BoardDeltaVZ = (board.linearVelocity.z - lastEntry.BoardVelocityZ) / Time.fixedDeltaTime;
            }
            
            // add current data to time series
            timeSeries.Add(new TimeSeriesData
            {
                Time = currentTime,
                DartPositionY = dart.position.y,
                DartPositionZ = dart.position.z,
                DartVelocityZ = dart.linearVelocity.z,
                DartVelocityY = dart.linearVelocity.y,
                DartDeltaVY = DartDeltaVY,
                DartDeltaVZ = DartDeltaVZ,
                BoardPositionY = board.position.y,
                BoardPositionZ = board.position.z,
                BoardVelocityY = board.linearVelocity.y,
                BoardVelocityZ = board.linearVelocity.z,
                BoardDeltaVY = BoardDeltaVY,
                BoardDeltaVZ = BoardDeltaVZ
            });

            // increase the current time step
            currentTime += Time.fixedDeltaTime;
        }
    }

    // Reload the scene
    private void ReloadScene()
    {
        // reload scene
        SceneManager.LoadScene(SceneManager.GetActiveScene().name);

        // reset time series
        timeSeries.Clear();
        currentTime = 0;
    }

    // Launch the dart
    private void LaunchDart()
    {
        if (!isLaunched)
        {
            // remember that the dart has been launched
            isLaunched = true;
        }
    }

    // Write the time series to a file
    void WriteTimeSeriesToCSV()
    {
        using var streamWriter = new StreamWriter("time_series.csv");
        streamWriter.WriteLine(TimeSeriesData.Header());

        foreach (TimeSeriesData timeStep in timeSeries)
        {
            streamWriter.WriteLine(timeStep.ToString());
            streamWriter.Flush();
        }

        // log
        Debug.Log("Time series data has been saved to time_series.csv");
    }
}


// Data structure to store the time series data
public class TimeSeriesData
{
    public float Time { get; set; }
    public float DartPositionY { get; set; }
    public float DartPositionZ { get; set; }
    public float DartVelocityY { get; set; }
    public float DartVelocityZ { get; set; }
    public float DartDeltaVY { get; set; }
    public float DartDeltaVZ { get; set; }
    public float BoardPositionY { get; set; }
    public float BoardPositionZ { get; set; }
    public float BoardVelocityY { get; set; }
    public float BoardVelocityZ { get; set; }
    public float BoardDeltaVY { get; set; }
    public float BoardDeltaVZ { get; set; }

    

    public override string ToString()
    {
        return $"{Time},{DartPositionY},{DartPositionZ},{DartVelocityY},{DartVelocityZ},{DartDeltaVY},{DartDeltaVZ},{BoardPositionY},{BoardPositionZ},{BoardVelocityY},{BoardVelocityZ},{BoardDeltaVY},{BoardDeltaVZ}";
    }

    public static string Header()
    {
        return "t, y_Dart, z_Dart, v_y_Dart, v_z_Dart, d_v_y_Dart, d_v_z_Dart, y_Board, z_Board, v_y_Board, v_z_Board, d_v_y_Board, d_v_z_Dart";
    }
}

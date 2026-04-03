using NUnit.Framework;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.SceneManagement;
using UnityEngine.VFX;

/**
 * Done by KPP (pern) in 2025!
 */

public class Car : MonoBehaviour
{
    // the rigid body of the car
    private Rigidbody rb;

    // the rigid body of the bumpers
    private Rigidbody leftBumper;
    private Rigidbody rightBumper;

    // the Exporter script (if attached to the game object)
    private Exporter exporter;

    // the time the car was launched
    private float launchTime;

    // flag to remember if car was launched
    private bool isLaunched = false;

    // the width of the car (could also be gotten from the collider)
    private readonly float carWidth = 0.3f;

    // the width of the bumpers (could also be gotten from the collider)
    private readonly float bumperWidth = 0.1f;

    // determines the state of the bumpers
    // 0: both bumpers are fixed
    // 1: left bumper is free to move, no friction
    // 2: left bumper is free to move, with friction
    public readonly int bumperMode = 1;

    // helper flag to automatically start the car when recording starts
    // Window > General > Recorder > Start Recording
    private readonly bool recording = true;


    // initial velocity of the car
    public float initialVelocity = 0f;

    // the length of the uncompressed spring
    public float springLength = 0.15f;

    // spring constant
    public float springConstant = 10f;

    // friction coefficient bumper (laminare viskose Dämpfung FR=frictionCoefficient * v)
    public float frictionCoefficient = 0.5f;


    // Start is called once before the first execution of Update after the MonoBehaviour is created
    void Start()
    {
        // get the rigid body of the car
        rb = GetComponent<Rigidbody>();

        // get the rigid body of the bumpers
        leftBumper = GameObject.Find("Bumper left").GetComponent<Rigidbody>();
        rightBumper = GameObject.Find("Bumper right").GetComponent<Rigidbody>();

        // get the Exporter script
        exporter = GetComponent<Exporter>();
        Assert.IsNotNull(exporter, "Exporter script not found");


        // confine motion of the to 1D along z-axis
        rb.constraints = RigidbodyConstraints.FreezePositionX | RigidbodyConstraints.FreezePositionY | RigidbodyConstraints.FreezeRotationX | RigidbodyConstraints.FreezeRotationY | RigidbodyConstraints.FreezeRotationZ;

        // set motion of the bumpers
        // Your code here ...
        switch (bumperMode)
        {
            case 0:
                //fix bumper in place
                leftBumper.constraints = RigidbodyConstraints.FreezeAll;
                break;
            case 1:
            case 2:
                //allow bumper to move along z-axis
                leftBumper.constraints = RigidbodyConstraints.FreezePositionX | RigidbodyConstraints.FreezePositionY |
                                         RigidbodyConstraints.FreezeRotationX | RigidbodyConstraints.FreezeRotationY |
                                         RigidbodyConstraints.FreezeRotationZ;
                break;
            default:
                Assert.Fail("Invalid bumper mode");
                break;
        }

        rightBumper.constraints = RigidbodyConstraints.FreezeAll;

        // Note: switch off collider in the inspector, because depending on the spring paramters, it can happen that the car touches the bumpers
        // and then the movement looks very strange and debugging is difficult (try increasing initial velocity). Without collider one sees
        // that the car penetrates the bumpers and debugging becomes easier.


        // === solver settings ===

        // Controls how often physics updates occur (default: 0.02s or 50 Hz)
        Time.fixedDeltaTime = 0.02f;

        // Determines how many times Unity refines the constraint solving per physics step(default: 6)
        Physics.defaultSolverIterations = 6;

        // Similar to above but specifically for velocity constraints (default: 1)
        Physics.defaultSolverVelocityIterations = 1;
    }



    // Update is called once per frame
    void Update()
    {
        // launch car
        if ((Keyboard.current[Key.Space].wasPressedThisFrame || recording) && !isLaunched)
        {
            // remember that car was launched
            isLaunched = true;

            // remember the current time
            launchTime = Time.time;

            // Your code here ...
            // 🚀 Set initial velocity (along z-axis)
            rb.linearVelocity = new Vector3(0f, 0f, initialVelocity);


            // log
            Debug.Log("Launching the car with bumper mode " + bumperMode + " at time " + launchTime);
        }


        // reload scene
        if (Keyboard.current[Key.R].wasPressedThisFrame)
        {
            // Reload the scene
            SceneManager.LoadScene(SceneManager.GetActiveScene().name);
        }
    }


    private void FixedUpdate()
    {
        // Your code here ...
        float compressionLeft = 0f;
        float forceLeft = 0f;
        float compressionRight = 0f;
        float forceRight = 0f;

        float deltaLeft = transform.position.z - leftBumper.position.z - carWidth / 2 - bumperWidth / 2;
        float deltaRight = transform.position.z - rightBumper.position.z + carWidth / 2 + bumperWidth / 2;

        if (bumperMode == 0)
        {
            // Left and right springs
            compressionLeft = springLength - deltaLeft;
            if (compressionLeft < 0)
            {
                compressionLeft = 0;
            }
            forceLeft = springConstant * compressionLeft;
            rb.AddForce(new Vector3(0f, 0f, forceLeft));

            compressionRight = springLength + deltaRight;
            if (compressionRight < 0)
            {
                compressionRight = 0;
            }
            forceRight = springConstant * compressionRight;
            rb.AddForce(new Vector3(0f, 0f, -forceRight));
        }
        else
        {
            if (deltaLeft <= 0 && rb.linearVelocity.z < 0) // Collision detected
            {
                // Get masses and velocities
                float m1 = rb.mass; // Mass of the car
                float m2 = leftBumper.mass; // Mass of the left bumper
                float v1 = rb.linearVelocity.z; // Velocity of the car
                float v2 = leftBumper.linearVelocity.z; // Velocity of the left bumper

                // Calculate new velocities after elastic collision
                float v1New = ((m1 - m2) / (m1 + m2)) * v1;
                float v2New = ((m2 * m1) / (m1 + m2)) * v1;

                // Apply new velocities
                rb.linearVelocity = new Vector3(0f, 0f, v1New);
                leftBumper.linearVelocity = new Vector3(0f, 0f, v2New);
            }

            if (bumperMode == 2)
            {
                // friction force on the left bumper 
                Vector3 frictionForce = -frictionCoefficient * leftBumper.linearVelocity.normalized * leftBumper.linearVelocity.magnitude;
                leftBumper.AddForce(frictionForce);
            }

            //// elastischer Stoß mit festem rechtem Bumper
            if (deltaRight >= 0)
            {
                Vector3 vel = rb.linearVelocity;
                if (vel.z > 0) // nur wenn Auto auf Bumper zufährt
                {
                    vel.z = -vel.z;
                    rb.linearVelocity = vel;
                    Debug.Log("Elastischer Stoß am rechten Bumper");
                }
            }
        }


        // === time series data
        // store time series record
        if (isLaunched)
        {
            // Your code here ... (adapt as needed)
            TimeSeriesData timeSeriesData = new TimeSeriesData(
                rb,
                Time.time - launchTime,
                compressionLeft,
                forceLeft,
                compressionRight,
                forceRight,
                leftBumper.position.z,
                leftBumper.linearVelocity.z
             );

            exporter.AddData(timeSeriesData);
        }
    }

    void OnGUI()
    {
        GUIStyle textStyle = new()
        {
            fontSize = 20,
            normal = { textColor = Color.black }
        };

        // keyboard shortcuts
        GUI.Label(new Rect(10, Screen.height - 20, 400, 20),
            "R ... Reload", textStyle);
    }
}

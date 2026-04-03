using System;
using System.Collections;
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
    public int bumperMode = 2;

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
        rb.constraints = RigidbodyConstraints.FreezePositionY |
                         RigidbodyConstraints.FreezeRotationX |
                         RigidbodyConstraints.FreezeRotationZ;

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

        rb.inertiaTensor = new Vector3(0, 0.0634f, 0);
        rb.inertiaTensorRotation = Quaternion.identity;

    }


    // Update is called once per frame
    void Update()
    {
        // launch car
        if (Keyboard.current[Key.Space].wasPressedThisFrame || (recording && !isLaunched))
        {
            // remember that car was launched
            isLaunched = true;

            // remember the current time
            launchTime = Time.time;

            // Your code here ...
            // 🚀 Set initial velocity (along z-axis)
            rb.linearVelocity = new Vector3(0f, 0f, initialVelocity);


            // log
            Debug.Log("Launching the car");
        }


        // reload scene
        if (Keyboard.current[Key.R].wasPressedThisFrame)
        {
            // Reload the scene
            SceneManager.LoadScene(SceneManager.GetActiveScene().name);
        }
    }

    private bool hasHitLeftBumper = false;
    private Collision leftBumperCollision = null;

    void OnCollisionEnter(Collision collision)
    {
        if (collision.gameObject.name == "Bumper left" && isLaunched && GetComponent<FixedJoint>() == null)
        {
            hasHitLeftBumper = true;
            leftBumperCollision = collision; // merken für FixedUpdate()
        }
    }


    private void FixedUpdate()
    {
        // Your code here ...
        float compressionLeft = 0f;
        float forceLeft = 0f;
        float compressionRight = 0f;
        float forceRight = 0f;

        // === left bumper ===

        // === inelastischer Stoß mit beweglichem linken Bumper

        if (hasHitLeftBumper && GetComponent<FixedJoint>() == null)
        {
            FixedJoint joint = gameObject.AddComponent<FixedJoint>();
            joint.connectedBody = leftBumperCollision.rigidbody;
            joint.breakForce = Mathf.Infinity;
            joint.breakTorque = Mathf.Infinity;
            joint.enableCollision = true;
            joint.enablePreprocessing = false;

            Debug.Log("FixedJoint gesetzt – Inelastischer Stoß mit linkem Bumper");

            hasHitLeftBumper = false; // einmalige Ausführung
        }
        

        // === right bumper ===

        // === elastischer Stoß mit festem rechtem Bumper
        if (transform.position.z >= rightBumper.position.z - carWidth / 2 - bumperWidth / 2)
        {
            Vector3 vel = rb.linearVelocity;
            if (vel.z > 0) // nur wenn Auto auf Bumper zufährt
            {
                vel.z = -vel.z;
                rb.linearVelocity = vel;
                Debug.Log("Elastischer Stoß am rechten Bumper");
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
                leftBumper.linearVelocity.z,
                rb.angularVelocity.y,
                rb.inertiaTensor.y
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

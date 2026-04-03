using UnityEngine;
using UnityEngine.InputSystem;

/**
 * Done by KPP (pern) in 2025!
 */

public class MoveDart : MonoBehaviour
{
    // reference to the Rigidbody component this script is assigned to
    public Rigidbody rb;

    // set initial velocity of the dart in the inspector (a positiv value points against the z-axis towards the target)
    public float initialVelocity = 6.0f; // in m/s

    // flag that indicates if the dart has been launched
    private bool isLaunched = false;

    // remember when dart hit the board
    public bool hitBoard = false;

    // Input action for launching the dart
    private InputAction launchAction;

    // Start is called before the first frame update
    void Start()
    {
        // get the Rigidbody component if not set in the Inspector
        if (rb == null)
            rb = GetComponent<Rigidbody>();

        // Ensure it's not moving at start
        rb.isKinematic = true;

        // Initialize the input action
        launchAction = new InputAction(binding: "<Keyboard>/space");
        launchAction.performed += ctx => LaunchDart();
        launchAction.Enable();
    }

    // Update is called once per frame
    void Update()
    {
    }

    // FixedUpdate is called once per frame at a defined interval (typically 20 ms)
    void FixedUpdate()
    {
        if (isLaunched)
        {
            // add gravitational force
            rb.AddForce(Physics.gravity, ForceMode.Acceleration);

        }
    }

    // called when the dart collides with another object
    private void OnCollisionEnter(Collision collision)
    {
        // log
        Debug.Log("Collision detected with: " + collision.gameObject.name);

        // check if the colliding body is the board
        // Note: The Board tag was added to the dart board game object in Unity
        if (collision.gameObject.CompareTag("Board"))
        {
            // fix the body (dart) in space
            rb.isKinematic = true;

            // fix the colliding body (dart board) in space
            collision.gameObject.GetComponent<Rigidbody>().isKinematic = true;

            // remember that the dart hit the board
            hitBoard = true;
        }
    }

    // Called when the user presses the space bar; launches the dart
    private void LaunchDart()
    {
        if (!isLaunched)
        {
            // launch dart
            isLaunched = true;
            rb.isKinematic = false;

            // give the dart an initial velocity
            //rb.linearVelocity = new Vector3(0, 0, -initialVelocity);
            //rb.AddForce(new Vector3(0, 0, -initialVelocity), ForceMode.Acceleration);
            rb.linearVelocity = Vector3.back * initialVelocity;
        }
    }
}

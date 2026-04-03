using UnityEngine;
using UnityEngine.InputSystem;

/**
 * Done by KPP (pern) in 2025!
 */

public class MoveDartBoard : MonoBehaviour
{
    // reference to the Rigidbody component this script is assigned to
    public Rigidbody rb;

    // flag that indicates if the dart has been launched
    private bool isLaunched = false;

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
        launchAction.performed += ctx => LaunchDartBoard();
        launchAction.Enable();
    }

    // Update is called once per frame
    void Update()
    {

    }

    // FixedUpdate is called once per frame at a defined interval (typically 20 ms)
    void FixedUpdate()
    {
        // add gravitational force
        rb.AddForce(Physics.gravity, ForceMode.Acceleration);
    }


    // Called when the user presses the space bar; launches the dart board
    private void LaunchDartBoard()
    {
        if (!isLaunched)
        {
            // launch dart board
            //Physics.gravity = Vector3.down * 0.5f;
            isLaunched = true;
            rb.isKinematic = false;
        }
    }
}

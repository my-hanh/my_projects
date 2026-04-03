using UnityEngine;
using UnityEngine.InputSystem;

/**
 * Done by KPP (pern) in 2025!
 */

public class OrbitCamera : MonoBehaviour
{
    // the ridig body the camera points to
    // needs to be assigned in the scene view
    public Transform target;

    // initial camera position
    public float yaw = 42.0f;
    public float pitch = 5.0f;
    public float distance = 6f;

    // Limits for camera rotation
    // Vector2 is misused as a tuple here so that Unity's can display the values in the Inspector
    public Vector2 yawLimits = new(0, 90);
    public Vector2 pitchLimits = new(-89.999f, 0);
    public Vector2 distanceLimits = new(1.0f, 10.0f);

    // Orbit and zoom sensitivity
    public float keyboardRotationSpeed = 50.0f;
    public float keyboardZoomSpeed = 2.0f;

    private PlayerInput playerInput;
    private InputAction moveAction;
    private InputAction zoomAction;

    // Start is called before the first frame update
    void Start()
    {
        if (target == null)
        {
            Debug.LogError("OrbitCamera: No target assigned!");
            enabled = false;
            return;
        }

        // Initialize Input Actions
        playerInput = GetComponent<PlayerInput>();
        if (playerInput == null)
        {
            Debug.LogError("OrbitCamera: No PlayerInput component found!");
            enabled = false;
            return;
        }

        //// Assign the input actions from the Input System asset
        //playerInput.actions = Resources.Load<InputActionAsset>("InputSystem");

        moveAction = playerInput.actions["Move"];
        if (moveAction == null)
        {
            Debug.LogError("OrbitCamera: No Move action found in PlayerInput!");
            enabled = false;
            return;
        }

        zoomAction = playerInput.actions["Zoom"];
        if (zoomAction == null)
        {
            Debug.LogError("OrbitCamera: No Zoom action found in PlayerInput!");
            enabled = false;
            return;
        }

        Debug.Log($"Initial pitch: {pitch:.0}   yaw: {yaw:.0}   distance: {distance}");
        transform.LookAt(target.position);
    }

    // Update is called once per frame
    void Update()
    {
        if (target == null) return;

        // Handle rotation (Keyboard)
        Vector2 moveInput = moveAction.ReadValue<Vector2>();
        yaw += moveInput.x * keyboardRotationSpeed * Time.deltaTime;
        pitch -= moveInput.y * keyboardRotationSpeed * Time.deltaTime;

        // Handle zoom (Keyboard)
        float zoomInput = zoomAction.ReadValue<float>();
        distance -= zoomInput * keyboardZoomSpeed * Time.deltaTime;

        // clamp distance, pitch and yaw to stay within the defined range
        // (could probalby also be done in inputSystem)
        distance = Mathf.Clamp(distance, distanceLimits.x, distanceLimits.y);
        pitch = Mathf.Clamp(pitch, pitchLimits.x, pitchLimits.y);
        yaw = Mathf.Clamp(yaw, yawLimits.x, yawLimits.y);

        // log
        //Debug.Log($"pitch: {pitch:.0}   yaw: {yaw:.0}    distance: {distance}");

        // Calculate the new camera position
        Quaternion rotation = Quaternion.Euler(pitch, yaw, 0);
        Vector3 offset = rotation * new Vector3(0, 0, distance);
        transform.position = target.position + offset;

        // Always look at the initial target position
        transform.LookAt(target);
    }
}

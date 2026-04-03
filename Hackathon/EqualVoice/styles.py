USER_TEXT_INITIAL_HEIGHT = 400
INITIAL_TEXT_WIDTH = "100%"
TEXT_WIDTH_WITH_DETAILS = "70%"
INITIAL_COMMENT_WIDTH = "0"
COMMENT_WIDTH_WITH_DETAILS = "30%"

# Custom styling
style_1 = (".text-with-results {" +
                "height: " +
                f"{USER_TEXT_INITIAL_HEIGHT}px;" + 
                f"width: {INITIAL_TEXT_WIDTH};" + 
                "border: 1px solid grey;" + 
                "border-radius: 5px;" + 
                "padding: 5px;" + 
                "margin: 5px;" +
                "overflow-y: scroll;"
                "}")
style_2 = """.highlighted {
                background-color: #d5c8fb;
                cursor: pointer;
            }"""
style_3 = (".comment-container {" +
                "height: " +
                f"{USER_TEXT_INITIAL_HEIGHT}px;" + 
                f"width: {INITIAL_COMMENT_WIDTH};" + 
                "overflow-y: scroll;"
                "}")
style_4 = (".comment-container p {" +
                "border: 1px solid grey;" + 
                "border-radius: 5px;" + 
                "padding: 5px;" + 
                "margin: 5px;" +
                "display: none;" +
                "}")
style_5 = """.horizontal {
                display: flex;
                flex-direction: row;
                justify-content: flex-start;
            }"""

custom_styles = [
    style_1,
    style_2,
    style_3,
    style_4,
    style_5,
]
import streamlit as st
import streamlit.components.v1 as components
from algorithm import analyze
from response_parser import map_marked_text_to_html, get_comments_as_html
from styles import custom_styles, USER_TEXT_INITIAL_HEIGHT, TEXT_WIDTH_WITH_DETAILS, COMMENT_WIDTH_WITH_DETAILS



# Set page configuration
st.set_page_config(
    page_title="Media Bias Tool",
    page_icon="📊",
    layout="wide",
)

# Create a sidebar for navigation
st.sidebar.title("Navigation")
if st.sidebar.button("🔍Analyze"):  
    st.switch_page("app.py")
if st.sidebar.button("📊Statistics"):  
    st.switch_page("pages/statisticPage.py")
if st.sidebar.button("📚Educational material"):  
    st.switch_page("pages/educationPage.py")
if st.sidebar.button("🎮Exercise"): 
    st.switch_page("pages/gamePage.py")

"In order to test the solution, you will need an OpenAI API key"
openai_api_key = st.text_input("OpenAI API Key", key="openai_api_key", type="password")
"[Get an OpenAI API key](https://platform.openai.com/account/api-keys)"


# Main content area
username = "Stella Muster"
st.title(f"Welcome, {username} 👤")
st.write("---")



# Initial values of some state variables
if "display_result" not in st.session_state:
    st.session_state.display_result = False
if "user_text" not in st.session_state:
    st.session_state.user_text = """Accusing some of her male colleagues of sexism, Los Angeles Councilwoman Laura Chick lashed out at City Hall on Thursday as the “most sexist, good-old-boys work environment that I’ve ever been in.”
Chick, one of four women on the 15-member panel, made the comments during a luncheon honoring women police officers, an event attended by Mayor Richard Riordan, Police Chief Willie L. Williams and several other top city officials.
The comment drew isolated applause from the audience of 130 or so administrators, police commanders and other guests, some of whom praised Chick after the luncheon for her comments. About half were women.
During her address, Chick told the women officers that she understands what it is like to work in “an arena that has traditionally and historically been one for men.”
When the audience responded with hushed murmurs to her remarks, Chick said: “Have I shocked you?”
In an interview after the luncheon, Chick said that in the two years she has been in City Hall some of her colleagues have made her feel uncomfortable by making “off-color” jokes, passing “dirty pictures and dirty cartoons” and making condescending remarks during council meetings.
For example, Chick said that on at least one occasion when a female council member stood to speak out against a proposal in the council chambers, a male council member was heard saying: “It must be that time of month again.” She declined to name any of the men.
Chick, who represents the west San Fernando Valley, suggested that all council members be required to take “gender sensitivity” training, similar to courses being given to top managers of the Los Angeles police and fire departments.
Some council members--both men and women--and female department heads echoed Chick’s comments. But no one was willing to identify the men they think have perpetuated the “good-old-boys work environment.”
“She is absolutely right,” said Councilwoman Rita Walters, who has been on the council for four years and also attended the luncheon. “It’s very much the old boys club.”
Walters said that even male department heads, who answer to the City Council, have spoken to her and other female council members in a condescending tone, indicating to her that they believe “the issues that female council members espouse are ... not serious issues.”
A top female city official who frequently appears before the council agreed that the environment in City Hall has made her uncomfortable.
“There are many instances where the male members of the council have had better access to information than female members,” said the official, who asked not to be named.
But some city officials said the environment is not much different from that of any large business throughout the nation, and otheres say the atmosphere in City Hall is improving.
Councilwoman Ruth Galanter, an eight-year council veteran, also described City Hall as “themost sexist work environment I’ve ever been in.” But she said City Hall is reflective of the atmosphere in many places outside government.
“It’s a microcosm of society,” she said."""


# Add custom styling
st.write(
    f'''<style>{''.join(custom_styles)}</style>''',
    unsafe_allow_html=True
)



#st.title("Text Bias Detector")

if not st.session_state.display_result:
    # Show empty textarea for user to enter the initial data.
    # We will hide it and replace with another more complex element for the results.
    user_text = st.text_area(
        label="",
        value=st.session_state.user_text,
        height= USER_TEXT_INITIAL_HEIGHT,  # Let CSS control height
        max_chars=None,
        key=None,
        help="Enter your text here.",
        on_change=None,
        args=None,
        kwargs=None,
        placeholder="Type your article here...",
        disabled=False,
        label_visibility="visible"
    )
else:
    # Show the text with highlights
    html_with_highlights, all_problems = map_marked_text_to_html(st.session_state.marked_text)
    comments_as_html = get_comments_as_html(st.session_state.all_comments)

    st.write(f"""<div class='horizontal'><div class='text-with-results' id='text-with-results'>{html_with_highlights}</div><div class='comment-container' id='comment-container'>{comments_as_html}</div></div>""", unsafe_allow_html=True)

    js = '''<script>
        highlights = window.parent.document.getElementsByClassName("highlighted");
        for (let i = 0; i < highlights.length; i++) {
            let element = highlights[i];
            element.addEventListener("click", () => {
              console.log('hello');
              window.parent.document.getElementById("text-with-results").style.width = "''' + TEXT_WIDTH_WITH_DETAILS + '''";
              window.parent.document.getElementById("comment-container").style.width = "''' + COMMENT_WIDTH_WITH_DETAILS + '''";
              for (let j = 0; j < highlights.length; j++) {
                let commentsDiv = window.parent.document.getElementById("comment-id-" + j);
                if (i === j) {
                    commentsDiv.style.display = 'block';
                } else {
                    commentsDiv.style.display = 'none';
                }
              }
            });
        }
        </script>
        '''
    components.html(f'''{js}''', height=0)


    



def clear_state():
    st.session_state.marked_text = None
    st.session_state.global_feedback = None
    st.session_state.all_comments = None
    st.session_state.display_result = False


col1, col2, col3 = st.columns([1, 4, 1])

if st.session_state.display_result:
    with col1:
        if st.button(
            label="Back",
            key=None,
            help="Click to go back.",
            on_click=None,
            args=None,
            kwargs=None,
            type="secondary",  
            icon=None,
            disabled=False,
            use_container_width=False
        ):
            clear_state()
            st.rerun()
            
    with col3:
        if st.button(
            label="Publish",  
            key=None,
            help="Click to publish your text.",
            on_click=None,
            args=None,
            kwargs=None,
            type="primary",  
            icon=None,
            disabled=False,
            use_container_width=False
        ):
            st.write("Published")
        
    # Show global feedback
    st.write("### Global feedback")
    st.write(st.session_state.global_feedback)

    #To cancel later
    #show all individual comments
    # st.write("### Breakdown of all possible bias")
    # st.write(st.session_state.all_comments)

    # #show the text
    # st.write("### Text with separators")
    # st.write(st.session_state.marked_text)

else:
    with col3:
        if st.button(
            label="Analyze",  
            key=None,
            help="Click to analyze your data.",
            on_click=None,
            args=None,
            kwargs=None,
            type="primary",  
            icon=None,
            disabled=False,
            use_container_width=False
        ):
            if not openai_api_key:
                st.toast("Please provide an OpenAI API key, there is a field at the top of the page", icon="⚠️")
            else:
                st.session_state.user_text = user_text
                marked_text, global_feedback, all_comments = analyze(user_text, openai_api_key)
                st.session_state.marked_text = marked_text
                st.session_state.global_feedback = global_feedback
                st.session_state.all_comments = all_comments
                st.session_state.display_result = True
        
                st.rerun()




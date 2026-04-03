from openai import OpenAI
import json
from ast import literal_eval


BIAS_SURROUNDING_CHAR = "|"


# STUB data, remove when analyze is implemented

#marked_text = f"The {BIAS_SURROUNDING_CHAR}cyclist's wife{BIAS_SURROUNDING_CHAR} won a gold medal.\n" + f"The {BIAS_SURROUNDING_CHAR}cyclist's wife{BIAS_SURROUNDING_CHAR} won a gold medal.\n" + f"The {BIAS_SURROUNDING_CHAR}cyclist's wife{BIAS_SURROUNDING_CHAR} won a gold medal."
# global_feedback = "It looks great"

# The cyclist's wife won a gold medal.

def get_response_from_prompt(client, model, messages, kwargs):
    response = client.chat.completions.create(
        model=model,
        messages=messages,
        n=1,
        **kwargs
    )
    return response.choices[0].message.content
    
    

def analyze(user_text, openai_api_key):
    """
    

    Parameters
    ----------
    user_text : string
        original text entered by the user

    Returns
    -------
    marked_text : string
        parsed original text with biased sections marked in between BIAS_SURROUNDING_CHAR (ex: |The cyclist's wife won a gold medal|)
    global_feedback : string
        Global feedback about the original text
    individual_comments : list of Python dictionnary
        {
             "Text": "<Content of the problematic section">, \n
             "Possible bias": "<the possible gender or racial detected bias in this problematic section>" \n
             "Suggested Improvement": "<suggestion to improve the text ">
        }

    """
    
        
    #General Feedback
    no_bias_message = "No bias detected in your article. Great job!"
    prompt_1 = f"""I am studying how language in the media shape gender bias. 
    You are an educational tool for journalists to raise awareness about gender or racial biais 
    in their articles. The goal is to flag where's there is possible gender or racial bias in the text of their article 
    and to give suggestions for improvement to use more inclusive language. 
    \n Here is a text from an article: {user_text}.\n\n
    \n Please give me a short general feedback on 1. where there is possible gender or racial bias in this text,
    2. why it is problematic and 3. provide suggestions for improvement.
    If there is no detected bias, write as an output: {no_bias_message} """
    
    #Detailed Feedback
    separator = "------------"
    
    prompt_2 = f"""Here is a text from an article: {user_text}.\n\n
    Now parse the text of this article and surround parts with possible gender or racial bias 
    with {BIAS_SURROUNDING_CHAR}. 
    First output: give as an output the article text with the problematic parts separatd by {BIAS_SURROUNDING_CHAR}.\n
    Then, write {separator}
    Second output: go through the problematic sections in between {BIAS_SURROUNDING_CHAR} one by one, 
    and explain shortly why they are possible bias, and give example for improvement. \n\n 
    This second output must be an array listing all problematic sections, the possible bias
    and the suggested improvements. It must be in JSON format, with each element formatted like this: \n
    {{
         "Text": "<Content of the problematic section">, \n
         "Possible bias": "<the possible gender or racial detected bias in this problematic section>" \n
         "Suggested Improvement": "<suggestion to improve the text ">
    }} \n
    \n\n
    
    Write your final response with the following structure: \n
    Content of First output \n
    {separator} \n
    Content of Second output
    \n 
    Do not write 'First output' or 'Second output' in your answer, just write the content."""
    
    
    # Prepare messages for the OpenAI chat model, including a system message and the user prompt
    messages_1 = [
        {"role": "system", "content": "you are an editor."},  # System message to set the assistant's behavior
        {"role": "user", "content": prompt_1},  # Ask a global feedback for the article
        # {"role": "system", "content": prompt_2},  # Ask to parse the article and extract sections with potential bias
        # {"role": "system", "content": prompt_3}   # Ask to return a comment for each problematic section
    ]
    
    messages_2 = [
        {"role": "system", "content": "you are an editor."},  # System message to set the assistant's behavior
        {"role": "system", "content": prompt_2},  # Ask to parse the article and extract sections with potential bias
    ]
    

    
    kwargs = {'max_tokens': 2048,'temperature': 0.2}
    
    client = OpenAI(
    
        api_key= openai_api_key
    )
    model = "gpt-4o"
    
   
    global_feedback =  get_response_from_prompt(client, model, messages_1, kwargs)
    
    detailed_feedback = get_response_from_prompt(client, model, messages_2, kwargs)
    
    #extract the marked text from ChatGPT response
    marked_text = detailed_feedback.split(sep=separator, maxsplit=-1)[0]
    
    #extract all individual comments
    individual_comments = detailed_feedback.split(sep=separator, maxsplit=-1)[1]
    individual_comments = individual_comments.replace('\n','')
    #transform into a list of Python dict
    try:
        list_of_comments= json.loads(individual_comments)
    except:
        print("Something went srong")
        individual_comments = individual_comments.replace('**Second Output:**', '')
        list_of_comments = individual_comments


    return marked_text, global_feedback, list_of_comments



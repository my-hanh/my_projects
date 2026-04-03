# -*- coding: utf-8 -*-
"""
Created on Sat Nov  2 09:55:44 2024

"""

import streamlit as st

# Create a sidebar for navigation
st.sidebar.title("Navigation")
if st.sidebar.button("🔍Analyze"):  
    st.switch_page("app.py")
if st.sidebar.button("📊Statistics"): 
    st.switch_page("pages/statisticPage.py")
if st.sidebar.button("📚Educational material"):  
    st.switch_page("pages/educationPage.py")
if st.sidebar.button("🎮Exercises"):  
    st.switch_page("pages/gamePage.py")

if st.button(
    label="Back", 
    key=None,
    help="",
    on_click=None,
    args=None,
    kwargs=None,
    type="secondary",  
    icon=None,
    disabled=False,
    use_container_width=True  
):
    st.switch_page("app.py")

st.title("Educational material")

st.write("Here you can read informations about biases.")
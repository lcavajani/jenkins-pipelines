#!/usr/bin/env python3

import argparse
import json
import os
import pprint
import re
import requests
import subprocess
import sys
from urllib.parse import urlsplit, urlunsplit
import yaml


def parse_args():
    """ Parse command line arguments """
    cli_argparser = argparse.ArgumentParser(description="Process args")
    cli_argparser.add_argument("--git-directory", "-d", required=False,
                               default="./", action="store",
                               help="git directory to check")
    cli_argparser.add_argument("--ci-jobfile", "-c", required=False,
                               default="ci.yaml", action="store",
                               help="Jenkins CI job file")
    cli_argparser.add_argument("--images-file", "-f", required=False,
                               default="files_repo.json", action="store",
                               help="Images file issued by list_image_repo.py")
    cli_argparser.add_argument("--auto", "-a", required=False,
                               default=False, action="store_true",
                               help="""
                                    Use this mode to automatically trigger
                                    all jobs with parameters for all platorms
                                    """)
    cli_argparser.add_argument("--platform", "-p", required=False,
                               default=None, action="store",
                               help="Specific platform to trigger jobs")
    cli_argparser.add_argument("--image", "-i", required=False,
                               default=None, action="store",
                               help="Image name for the platform")
    cli_argparser.add_argument("--image-url", "-u", required=False,
                               default=None, action="store",
                               help="Image URL for the platform")
    cli_argparser.add_argument("--dry-run", "-w", required=False,
                               default=False, action="store_true",
                               help="""
                                    Show what the script would do if 
                                    it was actually ran
                                    """)

    return cli_argparser.parse_args()


def filter_results(regex, file_list, action, ignore_case=False, replace_with=' '):
    """ Filter the file list base on regex """
    matched_files = []

    for f in file_list:
        if action == "match":
            if ignore_case:
                if re.match(regex, f, re.IGNORECASE):
                    matched_files.append(f)
            else:
                if re.match(regex, f):
                    matched_files.append(f)

        if action == "sub":
            f = re.sub(regex, replace_with, f)
            matched_files.append(f)

    return matched_files


def list_files_to_commit(git_dir):
    """ list untracked and modified files in a git directory """
    try:
        files = subprocess.check_output("git ls-files -m --other",
                                        shell=True, cwd=git_dir)
        files = re.sub('(?<=\\\\)n', ' ', str(files))
        files = re.sub('[(\\\\)(^b)(\')]', ' ', files)
        files = filter(None, files.split(' '))
        return files
    except subprocess.SubprocessError as e:
        sys.exit("ERROR: shelling out to git failed: {0}").format(e)


def open_file(filepath, language):
    """ Open file and read json/yaml """
    try:
        if language == "json":
            with open(filepath, "r", encoding="utf-8") as f:
                content = json.load(f)
        if language == "yaml":
            with open(filepath, "r", encoding="utf-8") as f:
                content = yaml.load(f)
        return(content)
    except IOError as e:
        sys.exit("ERROR: I/O error: {0}".format(e))
    except ValueError as ej:
        sys.exit("ERROR: Error in json file: {0}".format(ej))
    except yaml.YAMLError as ey:
        sys.exit("ERROR: Error in yaml file: {0}".format(ey))


def create_jenkins_job_baseurl(ci_config):
    """
    create the baseurl for jenkins jobs with the
    parameters if any, provided in the configuration file
    """
    jenkins_conf = ci_config["jenkins"]

    if jenkins_conf.get("url", None):
        base_url = jenkins_conf["url"]
    else:
        base_url = get_env_var("JENKINS_URL")

    base_url = urlsplit(base_url)

    # Define if we need to use a view
    view = jenkins_conf["view"]
    if view == "default":
        view_path = ""
    else:
        view_path = "view/" + view + "/"

    for platform in ci_config["jobs"]:
        for job in ci_config["jobs"][platform]:
            path = view_path + "job/" + job["name"] + "/buildWithParameters"

            # Add parameters to the build URL
            query = []
            if job.get("parameters", None):
                for k, v in job["parameters"].items():
                    query.append(str(k) + "=" + str(v))
            query = "&".join(query)

            url = (base_url.scheme, base_url.netloc, path, query, "")
            job["url"] = urlunsplit(url)

    return ci_config


def create_params_all_platforms(ci_config, image_files, changed_images):
    """
    Associate the build parameters (IMAGE, IMAGE_URL) to
    the jobs accordingly to the platforms
    """

    image_baseurl = image_files["url"]
    use_image = ci_config["parameters"]["image"]
    use_image_url = ci_config["parameters"]["image_url"]

    for platform in ci_config["jobs"]:
        # required to make sure xen use the correct image
        platform_regex = platform
        if platform == "xen":
            platform_regex = "(?<!kvm-and-)xen"

        for image in changed_images:
            if re.search(platform_regex, image, re.IGNORECASE):
                # create build parameters
                image_query = "IMAGE={0}".format(image)
                image_url_query = "IMAGE_URL={0}".format(image_baseurl + image)

                query = []
                if use_image:
                    query.append(image_query)
                if use_image_url:
                    query.append(image_url_query)

                params = "&".join(query)

                for job in ci_config["jobs"][platform]:
                    base_url = urlsplit(job["url"])

                    # only use for the outputs to show info                    
                    if not job.get("parameters", None):
                        job["parameters"] = {}

                    job["parameters"]["image"] = image
                    job["parameters"]["image_url"] = image_baseurl + image

                    # manage if default parameters are provided in config
                    if base_url.query:
                        query = base_url.query + "&" + params
                    else:
                        query = params

                    # add a key with the final url
                    url = (base_url.scheme, base_url.netloc,
                           base_url.path, query, "")
                    url = urlunsplit(url)
                    job["url"] = url

    return ci_config


def create_params_specific_platform(ci_config, platform, image, image_url):
    """
    Same as create_params_all_platforms() but with the
    build parameters provided from the command line
    """
    # create build parameters
    image_query = "IMAGE={0}".format(image)
    image_url_query = "IMAGE_URL={0}".format(image_url)

    params = "{0}&{1}".format(image_query, image_url_query)

    for job in ci_config["jobs"][platform]:
        base_url = urlsplit(job["url"])

        # manage if default parameters are provided in config
        if base_url.query:
            query = base_url.query + "," + params
        else:
            query = params

        # add a key with the final url
        url = (base_url.scheme, base_url.netloc,
               base_url.path, query, "")
        url = urlunsplit(url)
        job["url"] = url

    return ci_config


def http_post(url, auth=None, crumb=None):
    """ Send POST to an URL with authentication """
    try:
        http = requests.Session()
        request = http.post(url, auth=auth, data=crumb)
        request.raise_for_status()
        return request
    except (ConnectionError, requests.HTTPError, requests.Timeout) as e:
        sys.exit("connection failed: {0}".format(e))


def launch_jenkins_jobs(ci_config, auth, platform=None, dry_run=False):
    """ Trigger the jenkins jobs """
    print("INFO: CI jobs configuration")
    print(json.dumps(ci_config, indent=2))

    def run(job):
        url = job["url"]
        name = job["name"]
        print("INFO: launching job: {0}".format(name))
        print("INFO: sending POST to: {0}".format(url))
        if not dry_run:
            if auth:
                http_post(url,
                          auth=(auth["user"], auth["password"]),
                          crumb=auth.get("crumb", None))
            else:
                http_post(url)

    if platform:
        for job in ci_config["jobs"][platform]:
            run(job)
    else:
        for k, v in ci_config["jobs"].items():
            for job in v:
                run(job)


def get_env_var(envvar):
    """ Read environment variable """
    if os.environ.get(envvar, None):
        return os.environ[envvar]
    else:
        sys.exit("ERROR: {0} environment var is not set".format(envvar))


def retrieve_authentication(ci_config):
    """
    Retrieve authentication if enabled
        user/password if mandatory
        crumb looks like: Jenkins-Crumb:c57be33dbcb957f728b
    Values provided in files take precedence over env var
    """
    auth = {}
    auth_config = ci_config["jenkins"]["authentication"]

    if auth_config["enabled"]:
        if auth_config.get("user", None):
            auth["user"] = auth_config["user"]
        else:
            auth["user"] = get_env_var("JENKINS_USER")

        if auth_config.get("password", None):
            auth["password"] = auth_config["password"]
        else:
            auth["password"] = get_env_var("JENKINS_PASSWORD")

        # do not fail if CSRF is not enabled
        try:
            if auth_config.get("crumb", None):
                crumb = auth_config["crumb"]
            elif os.environ.get("JENKINS_CRUMB", None):
                crumb = get_env_var("JENKINS_CRUMB")
            else:
                crumb = None
        finally:
            if crumb:
                crumb = crumb.split(":")
                dict_crumb = {}
                dict_crumb[crumb[0]] = crumb[1]
                crumb = dict_crumb

            auth["crumb"] = crumb

    return auth


def main():
    args = parse_args()

    # load CI configuration and create build job url
    ci_config = open_file(args.ci_jobfile, "yaml")
    ci_config = create_jenkins_job_baseurl(ci_config)

    # authentication
    auth = retrieve_authentication(ci_config)

    dry_run = args.dry_run

    if dry_run:
        print("INFO: dry-run mode activated")

    if args.auto:
        # load image files
        image_files = open_file(args.images_file, "json")

        changed_images = list_files_to_commit(args.git_directory)
        changed_images = filter_results("^SUSE|^SLE", changed_images,
                                        action="match", ignore_case=True)
        changed_images = filter_results(".sha256", changed_images,
                                        action="sub", ignore_case=True,
                                        replace_with='')

        # select only image from the configured stack
        stack = ci_config["parameters"]["stack"]
        if stack == "caasp":
            stack_regex = ".*(caasp-stack)"

        if stack == "microos":
            stack_regex = ".*(microos)"

        changed_images = filter_results(stack_regex, changed_images,
                                        action="match", ignore_case=True)

        # associates new images as build parameters with platforms accordingly
        ci_config = create_params_all_platforms(ci_config,
                                                image_files,
                                                changed_images)
        # trigger all the jobs
        launch_jenkins_jobs(ci_config, auth, dry_run=dry_run)

    if not args.auto:
        ci_config = create_params_specific_platform(ci_config,
                                                    args.platform,
                                                    args.image,
                                                    args.image_url)

        # trigger the jobs for the platform
        launch_jenkins_jobs(ci_config, auth, args.platform, dry_run=dry_run)


if __name__ == "__main__":
    main()

import shanoir_downloader
import sys

parser = shanoir_downloader.create_arg_parser()
shanoir_downloader.add_arguments(parser)
args = parser.parse_args()
config = shanoir_downloader.initialize(args)
response = shanoir_downloader.solr_search(config, args)
shanoir_downloader.download_search_results(config, args, response)

# do whatever with filenames